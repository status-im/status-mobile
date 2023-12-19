(ns status-im2.contexts.chat.composer.events
  (:require [clojure.string :as string]
            [legacy.status-im.chat.models.mentions :as mentions]
            [legacy.status-im.data-store.messages :as data-store-messages]
            [status-im2.constants :as constants]
            [status-im2.contexts.chat.composer.link-preview.events :as link-preview]
            [status-im2.contexts.chat.messages.transport.events :as messages.transport]
            [taoensso.timbre :as log]
            [utils.emojilib :as emoji]
            [utils.i18n :as i18n]
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

(rf/defn set-input-content-height
  {:events [:chat.ui/set-input-content-height]}
  [{db :db} content-height chat-id]
  (let [current-chat-id (or chat-id (:current-chat-id db))]
    {:db (assoc-in db [:chat/inputs current-chat-id :input-content-height] content-height)}))

(rf/defn set-input-maximized
  {:events [:chat.ui/set-input-maximized]}
  [{db :db} maximized? chat-id]
  (let [current-chat-id (or chat-id (:current-chat-id db))]
    {:db (assoc-in db [:chat/inputs current-chat-id :input-maximized?] maximized?)}))

(rf/defn set-input-focused
  {:events [:chat.ui/set-input-focused]}
  [{db :db} focused? chat-id]
  (let [current-chat-id (or chat-id (:current-chat-id db))]
    {:db (assoc-in db [:chat/inputs current-chat-id :focused?] focused?)}))

(rf/defn set-input-audio
  {:events [:chat.ui/set-input-audio]}
  [{db :db} audio chat-id]
  (let [current-chat-id (or chat-id (:current-chat-id db))]
    {:db (assoc-in db [:chat/inputs current-chat-id :audio] audio)}))

(rf/defn set-recording
  {:events [:chat.ui/set-recording]}
  [{db :db} recording?]
  {:db (assoc db :chats/recording? recording?)})

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
    {:db         (-> db
                     (assoc-in [:chat/inputs current-chat-id :metadata :editing-message]
                               message)
                     (assoc-in [:chat/inputs current-chat-id :metadata :responding-to-message] nil)
                     (update-in [:chat/inputs current-chat-id :metadata]
                                dissoc
                                :sending-image))
     :dispatch-n [[:chat.ui/set-chat-input-text nil current-chat-id]
                  [:mention/to-input-field text current-chat-id]]}))

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

(defn build-text-message
  [{:keys [db]} input-text current-chat-id]
  (when-not (string/blank? input-text)
    (let [{:keys [message-id]}
          (get-in db [:chat/inputs current-chat-id :metadata :responding-to-message])
          preferred-name (get-in db [:profile/profile :preferred-name])
          emoji? (emoji-only-content? {:text        input-text
                                       :response-to message-id})]
      {:chat-id       current-chat-id
       :content-type  (if emoji?
                        constants/content-type-emoji
                        constants/content-type-text)
       :text          input-text
       :response-to   message-id
       :ens-name      preferred-name
       :link-previews (map #(select-keys % [:url :title :description :thumbnail])
                           (get-in db [:chat/link-previews :unfurled]))})))

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

(rf/defn send-audio-message
  {:events [:chat/send-audio]}
  [{:keys [db] :as cofx} audio-path duration]
  (let [{:keys [current-chat-id]} db
        {:keys [message-id]}
        (get-in db [:chat/inputs current-chat-id :metadata :responding-to-message])]
    (when-not (string/blank? audio-path)
      (rf/merge
       {:db (assoc-in db [:chat/inputs current-chat-id :metadata :responding-to-message] nil)}
       (messages.transport/send-chat-messages
        [(merge
          {:chat-id           current-chat-id
           :content-type      constants/content-type-audio
           :audio-path        audio-path
           :audio-duration-ms duration
           :text              (i18n/label :t/update-to-listen-audio {"locale" "en"})}
          (when message-id
            {:response-to message-id}))])))))

(rf/defn send-edited-message
  [{:keys [db]
    :as   cofx} text {:keys [message-id quoted-message chat-id]}]
  (rf/merge
   cofx
   {:json-rpc/call [{:method      "wakuext_editMessage"
                     :params      [{:id           message-id
                                    :text         text
                                    :content-type (if (emoji-only-content?
                                                       {:text        text
                                                        :response-to quoted-message})
                                                    constants/content-type-emoji
                                                    constants/content-type-text)
                                    :linkPreviews (map #(-> %
                                                            (select-keys [:url :title :description
                                                                          :thumbnail])
                                                            data-store-messages/->link-preview-rpc)
                                                       (get-in db [:chat/link-previews :unfurled]))}]
                     :js-response true
                     :on-error    #(log/error "failed to edit message " %)
                     :on-success  (fn [result]
                                    (rf/dispatch [:sanitize-messages-and-process-response
                                                  result]))}]}
   (link-preview/reset-unfurled)
   (cancel-message-edit)))

(rf/defn replace-mentions-and-send-current-message
  "Sends message from current chat input"
  {:events [:chat.ui/replace-mentions-and-send-current-message]}
  [{{:keys [current-chat-id] :as db} :db}]
  (let [{:keys [input-text metadata]} (get-in db [:chat/inputs current-chat-id])]
    {:json-rpc/call [{:method     "wakuext_chatMentionReplaceWithPublicKey"
                      :params     [current-chat-id input-text]
                      :on-error   #(log/error "[wakuext_chatMentionReplaceWithPublicKey] on-error" %)
                      :on-success #(rf/dispatch [:chat.ui/send-current-message-with-mentions
                                                 current-chat-id
                                                 (:editing-message metadata)
                                                 input-text
                                                 %])}]}))

(rf/defn send-current-message-with-mentions
  {:events [:chat.ui/send-current-message-with-mentions]}
  [{:keys [db] :as cofx} current-chat-id editing-message input-text new-text]
  (rf/merge cofx
            (if editing-message
              (send-edited-message new-text editing-message)
              (send-messages new-text current-chat-id))))
