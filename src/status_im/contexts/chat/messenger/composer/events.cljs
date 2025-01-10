(ns status-im.contexts.chat.messenger.composer.events
  (:require [clojure.string :as string]
            [legacy.status-im.chat.models.mentions :as mentions]
            [legacy.status-im.data-store.messages :as data-store.messages]
            [status-im.constants :as constants]
            [status-im.contexts.chat.messenger.composer.link-preview.events :as link-preview]
            [status-im.contexts.chat.messenger.messages.transport.events :as messages.transport]
            [taoensso.timbre :as log]
            [utils.debounce :as debounce]
            [utils.emojilib :as emoji]
            [utils.re-frame :as rf]
            utils.string))

(rf/defn set-chat-input-text
  {:events [:chat.ui/set-chat-input-text]}
  [{:keys [db] :as cofx} new-input chat-id]
  (let [current-chat-id (or chat-id (:current-chat-id db))]
    (rf/merge
     cofx
     {:db (assoc-in db [:chat/inputs current-chat-id :input-text] (emoji/text->emoji new-input))}
     (when (empty? new-input)
       (mentions/clear-mentions)))))

(rf/defn set-chat-input-ref
  {:events [:chat/set-input-ref]}
  [{:keys [db]} input-ref]
  (let [current-chat-id (:current-chat-id db)]
    {:db (assoc-in db [:chat/inputs current-chat-id :input-ref] input-ref)}))

(rf/defn set-input-focused
  {:events [:chat.ui/setnn-input-focused]}
  [{db :db} focused? chat-id]
  (let [current-chat-id (or chat-id (:current-chat-id db))]
    {:db (assoc-in db [:chat/inputs current-chat-id :focused?] focused?)}))

(rf/defn reply-to-message
  "Sets reference to previous chat message and focuses on input"
  {:events [:chat.ui/reply-to-message]}
  [{:keys [db]} message]
  (let [current-chat-id (:current-chat-id db)]
    {:db (-> db
             (assoc-in [:chat/inputs current-chat-id :metadata :responding-to-message]
                       message)
             (assoc-in [:chat/inputs current-chat-id :metadata :editing-message] nil)
             (update-in [:chat/inputs current-chat-id :metadata]
                        dissoc
                        :sending-image))}))

(rf/defn edit-message
  "Sets reference to previous chat message and focuses on input"
  {:events [:chat.ui/edit-message]}
  [{:keys [db]} message]
  (let [current-chat-id (:current-chat-id db)
        text            (get-in message [:content :text])]
    {:db       (-> db
                   (assoc-in [:chat/inputs current-chat-id :metadata :editing-message]
                             message)
                   (assoc-in [:chat/inputs current-chat-id :metadata :responding-to-message] nil)
                   (update-in [:chat/inputs current-chat-id :metadata]
                              dissoc
                              :sending-image))
     :dispatch [:mention/to-input-field text current-chat-id]}))

(rf/defn cancel-message-reply
  "Cancels stage message reply"
  {:events [:chat.ui/cancel-message-reply]}
  [{:keys [db]}]
  (let [current-chat-id (:current-chat-id db)]
    {:db (assoc-in db [:chat/inputs current-chat-id :metadata :responding-to-message] nil)}))

(rf/defn clean-input
  [{:keys [db] :as cofx} current-chat-id]
  (rf/merge cofx
            {:db (-> db
                     (assoc-in [:chat/inputs current-chat-id :metadata :sending-contact-request] nil)
                     (assoc-in [:chat/inputs current-chat-id :metadata :sending-image] nil)
                     (assoc-in [:chat/inputs current-chat-id :metadata :editing-message] nil)
                     (assoc-in [:chat/inputs current-chat-id :metadata :responding-to-message] nil))}
            (set-chat-input-text nil current-chat-id)))

(rf/defn cancel-message-edit
  "Cancels stage message edit"
  {:events [:chat.ui/cancel-message-edit]}
  [{:keys [db] :as cofx}]
  (let [current-chat-id (:current-chat-id db)]
    (rf/merge cofx
              (clean-input current-chat-id)
              (link-preview/reset-unfurled)
              (mentions/clear-mentions))))


(defn emoji-only-content?
  "Determines if text is just an emoji"
  [{:keys [text response-to]}]
  (and (not response-to)
       (string? text)
       (re-matches constants/regx-emoji text)))

(defn build-image-messages
  [{db :db} chat-id input-text]
  (let [images               (get-in db [:chat/inputs chat-id :metadata :sending-image])
        {:keys [message-id]} (get-in db [:chat/inputs chat-id :metadata :responding-to-message])
        album-id             (str (random-uuid))]
    (mapv (fn [[_ {:keys [resized-uri width height]}]]
            {:chat-id       chat-id
             :album-id      album-id
             :content-type  constants/content-type-image
             :image-path    (utils.string/safe-replace resized-uri #"file://" "")
             :image-width   width
             :image-height  height
             :text          input-text
             :link-previews (map #(select-keys % [:url :title :description :thumbnail])
                                 (get-in db [:chat/link-previews :unfurled]))
             :response-to   message-id})
          images)))

(defn- build-status-link-previews
  [status-link-previews]
  (map (fn [{{:keys [community-id color description display-name
                     members-count active-members-count icon banner]}
             :community
             url :url}]
         {:url url
          :community
          {:community-id         community-id
           :color                color
           :description          description
           :display-name         display-name
           :members-count        members-count
           :active-members-count active-members-count
           :icon                 {:data-uri (:data-uri icon)
                                  :width    (:width icon)
                                  :height   (:height icon)}
           :banner               {:data-uri (:data-uri banner)
                                  :width    (:width banner)
                                  :height   (:height banner)}}})
       status-link-previews))

(defn- build-link-previews
  [link-previews]
  (->> link-previews
       (map #(select-keys %
                          [:url :title :description :thumbnail
                           :status-link-preview? :favicon]))
       (filter (fn [{:keys [status-link-preview?]}]
                 (not status-link-preview?)))))

(defn build-text-message
  [{:keys [db]} input-text current-chat-id]
  (when-not (string/blank? input-text)
    (let [{:keys [message-id]}
          (get-in db [:chat/inputs current-chat-id :metadata :responding-to-message])
          preferred-name (get-in db [:profile/profile :preferred-name])
          emoji? (emoji-only-content? {:text        input-text
                                       :response-to message-id})]
      {:chat-id              current-chat-id
       :content-type         (if emoji?
                               constants/content-type-emoji
                               constants/content-type-text)
       :text                 input-text
       :response-to          message-id
       :ens-name             preferred-name
       :link-previews        (build-link-previews (get-in db [:chat/link-previews :unfurled]))
       :status-link-previews (build-status-link-previews
                              (get-in db [:chat/status-link-previews :unfurled]))})))

(rf/defn send-messages
  [{:keys [db] :as cofx} input-text current-chat-id]
  (let [image-messages (build-image-messages cofx current-chat-id input-text)
        text-message   (when-not (seq image-messages)
                         (build-text-message cofx input-text current-chat-id))
        messages       (keep identity (conj image-messages text-message))]
    (when (seq messages)
      (rf/merge cofx
                (clean-input (:current-chat-id db))
                (link-preview/reset-unfurled)
                (messages.transport/send-chat-messages messages)))))

(defn- process-link-previews
  [link-previews]
  (->> link-previews
       (map #(select-keys %
                          [:url :title :description :thumbnail
                           :status-link-preview? :favicon]))
       (map data-store.messages/->link-preview-rpc)
       (filter (fn [{:keys [status-link-preview?]}]
                 (not status-link-preview?)))))

(rf/defn send-edited-message
  [{:keys [db]
    :as   cofx} text {:keys [message-id quoted-message]}]
  (rf/merge
   cofx
   {:json-rpc/call
    [{:method      "wakuext_editMessage"
      :params      [{:id                 message-id
                     :text               text
                     :content-type       (if (emoji-only-content?
                                              {:text        text
                                               :response-to quoted-message})
                                           constants/content-type-emoji
                                           constants/content-type-text)
                     :linkPreviews       (process-link-previews (get-in db
                                                                        [:chat/link-previews :unfurled]))
                     :statusLinkPreviews (map data-store.messages/->status-link-previews-rpc
                                              (get-in db
                                                      [:chat/status-link-previews
                                                       :unfurled]))}]
      :js-response true
      :on-error    #(log/error "failed to edit message " %)
      :on-success  (fn [result]
                     (rf/dispatch [:sanitize-messages-and-process-response
                                   result]))}]}
   (link-preview/reset-unfurled)
   (cancel-message-edit)))

(rf/defn send-current-message
  "Sends message from current chat input"
  {:events [:chat.ui/send-current-message]}
  [{{:keys [current-chat-id] :as db} :db :as cofx}]
  (let [{:keys [input-text metadata]} (get-in db [:chat/inputs current-chat-id])
        editing-message               (:editing-message metadata)]
    (debounce/clear :link-preview/unfurl-urls)
    (debounce/clear :mention/on-change-text)
    (if editing-message
      (send-edited-message cofx input-text editing-message)
      (send-messages cofx input-text current-chat-id))))

(rf/reg-fx :effects/set-input-text-value
 (fn [[input-ref text-value]]
   (when (and (not (string/blank? text-value)) input-ref)
     (.setNativeProps ^js input-ref (clj->js {:text (emoji/text->emoji text-value)})))))
