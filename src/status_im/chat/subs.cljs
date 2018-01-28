(ns status-im.chat.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [status-im.constants :as constants]
            [status-im.chat.models.input :as input-model]
            [status-im.chat.models.commands :as commands-model] 
            [status-im.chat.views.input.utils :as input-utils]
            [status-im.commands.utils :as commands-utils]
            [status-im.utils.datetime :as time]
            [status-im.utils.platform :as platform]
            [status-im.i18n :as i18n]
            [clojure.string :as string]))

(reg-sub :get-chats :chats)

(reg-sub :get-current-chat-id :current-chat-id)

(reg-sub :chat-ui-props :chat-ui-props)

(reg-sub
  :get-current-chat-ui-props
  :<- [:chat-ui-props]
  :<- [:get-current-chat-id]
  (fn [[chat-ui-props id]]
    (get chat-ui-props id)))

(reg-sub
  :get-current-chat-ui-prop
  :<- [:get-current-chat-ui-props]
  (fn [ui-props [_ prop]]
    (get ui-props prop)))

(reg-sub
  :validation-messages
  :<- [:get-current-chat-ui-props]
  (fn [ui-props]
    (some-> ui-props :validation-messages commands-utils/generate-hiccup)))

(reg-sub
  :result-box-markup
  :<- [:get-current-chat-ui-props]
  (fn [ui-props]
    (some-> ui-props :result-box :markup commands-utils/generate-hiccup)))

(reg-sub
  :chat-input-margin
  :<- [:get :keyboard-height]
  (fn [kb-height]
    (if platform/ios? kb-height 0)))

(reg-sub
  :get-active-chats
  :<- [:get-chats]
  (fn [chats]
    (into {} (filter (comp :is-active second)) chats)))

(reg-sub
  :get-chat
  :<- [:get-active-chats]
  (fn [chats [_ chat-id]]
    (get chats chat-id)))

(reg-sub
  :get-current-chat
  :<- [:get-active-chats]
  :<- [:get-current-chat-id]
  (fn [[chats current-chat-id]]
    (get chats current-chat-id)))

(reg-sub
  :get-current-chat-message
  :<- [:get-current-chat]
  (fn [{:keys [messages]} [_ message-id]]
    (get messages message-id)))

(reg-sub
  :chat
  :<- [:get-active-chats]
  :<- [:get-current-chat-id]
  (fn [[chats id] [_ k chat-id]]
    (get-in chats [(or chat-id id) k])))

(defn message-datemark-groups
  "Transforms map of messages into sequence of `[datemark messages]` tuples, where
  messages with particular datemark are sorted according to their `:clock-value` and
  tuples themeselves are sorted according to the highest `:clock-value` in the messages."
  [id->messages]
  (let [datemark->messages (transduce (comp (map second)
                                            (filter :show?)
                                            (map (fn [{:keys [timestamp] :as msg}]
                                                   (assoc msg :datemark (time/day-relative timestamp)))))
                                      (completing (fn [acc {:keys [datemark] :as msg}]
                                                    (update acc datemark conj msg)))
                                      {}
                                      id->messages)]
    (->> datemark->messages
         (map (fn [[datemark messages]]
                [datemark (sort-by :clock-value > messages)]))
         (sort-by (comp :clock-value first second) >))))

(reg-sub
  :get-chat-message-datemark-groups
  (fn [[_ chat-id]]
    (subscribe [:get-chat chat-id]))
  (fn [{:keys [messages]}]
    (message-datemark-groups messages)))

(defn messages-stream
  "Transforms message-datemark-groups into flat sequence of messages interspersed with
  datemark messages.
  Additionaly enhances the messages in message sequence with derived stream context information,
  like `:same-author?`, `:same-direction?`, `:last?` and `:last-outgoing?` flags. "
  [message-datemark-groups]
  (if (seq message-datemark-groups)
    (let [messages-seq (mapcat second message-datemark-groups)
          {last-message-id :message-id} (first messages-seq)
          {last-outgoing-message-id :message-id} (->> messages-seq
                                                      (filter :outgoing)
                                                      first)]
      (->> message-datemark-groups
           (mapcat (fn [[datemark messages]]
                     (let [prepared-messages (into []
                                                   (map (fn [{:keys [message-id] :as message} previous-message]
                                                          (assoc message
                                                                 :same-author?    (= (:from message)
                                                                                     (:from previous-message))
                                                                 :same-direction? (= (:outgoing message)
                                                                                     (:outgoing previous-message))
                                                                 :last?           (= message-id
                                                                                     last-message-id)
                                                                 :last-outgoing?  (= message-id
                                                                                     last-outgoing-message-id)))
                                                        messages
                                                        (concat (rest messages) '(nil))))]
                       (conj prepared-messages {:type :datemark
                                                :value datemark}))))))
    ;; when no messages are in chat, we need to at least fake-out today's datemark
    (list {:type  :datemark
           :value (i18n/label :t/datetime-today)})))

(reg-sub
  :get-current-chat-messages
  :<- [:get-current-chat]
  (fn [{:keys [messages]}]
    (-> messages message-datemark-groups messages-stream)))

(reg-sub
  :get-commands-for-chat
  :<- [:get-commands-responses-by-access-scope]
  :<- [:get-current-account]
  :<- [:get-current-chat]
  :<- [:get-contacts]
  (fn [[commands-responses account chat contacts]]
    (commands-model/commands-responses :command commands-responses account chat contacts)))

(reg-sub
  :get-responses-for-chat
  :<- [:get-commands-responses-by-access-scope]
  :<- [:get-current-account]
  :<- [:get-current-chat]
  :<- [:get-contacts]
  (fn [[commands-responses account {:keys [requests] :as chat} contacts]]
    (commands-model/requested-responses commands-responses account chat contacts (vals requests))))

(def ^:private map->sorted-seq (comp (partial map second) (partial sort-by first)))

(defn- available-commands-responses [[commands-responses {:keys [input-text]}]]
  (->> commands-responses
       map->sorted-seq
       (filter #(string/includes? (commands-model/command-name %) (or input-text "")))))

(reg-sub
  :get-available-commands
  :<- [:get-commands-for-chat]
  :<- [:get-current-chat]
  available-commands-responses)

(reg-sub
  :get-available-responses
  :<- [:get-responses-for-chat]
  :<- [:get-current-chat]
  available-commands-responses)

(reg-sub
  :get-available-commands-responses
  :<- [:get-commands-for-chat]
  :<- [:get-responses-for-chat]
  (fn [[commands responses]]
    (map->sorted-seq (merge commands responses))))

(reg-sub
  :selected-chat-command
  :<- [:get-current-chat]
  :<- [:get-commands-for-chat]
  :<- [:get-responses-for-chat]
  (fn [[chat commands responses]]
    (input-model/selected-chat-command chat commands responses)))

(reg-sub
  :current-chat-argument-position
  :<- [:selected-chat-command]
  :<- [:get-current-chat]
  :<- [:get-current-chat-ui-prop :selection]
  (fn [[command {:keys [input-text seq-arguments]} selection]]
    (input-model/current-chat-argument-position command input-text selection seq-arguments)))

(reg-sub
  :chat-parameter-box
  :<- [:get-current-chat]
  :<- [:selected-chat-command]
  :<- [:current-chat-argument-position]
  (fn [[current-chat selected-chat-command argument-position]]
    (cond
      (and selected-chat-command
           (not= argument-position input-model/*no-argument-error*))
      (get-in current-chat [:parameter-boxes
                            (get-in selected-chat-command [:command :name])
                            argument-position])

      (not selected-chat-command)
      (get-in current-chat [:parameter-boxes :message])

      :default
      nil)))

(reg-sub
  :show-parameter-box?
  :<- [:chat-parameter-box]
  :<- [:show-suggestions?]
  :<- [:get-current-chat]
  :<- [:validation-messages]
  (fn [[chat-parameter-box show-suggestions? {:keys [input-text]} validation-messages]]
    (and (get chat-parameter-box :markup)
         (not validation-messages)
         (not show-suggestions?))))

(reg-sub
  :command-completion
  :<- [:selected-chat-command]
  input-model/command-completion)

(reg-sub
  :show-suggestions?
  :<- [:get-current-chat-ui-prop :show-suggestions?]
  :<- [:get-current-chat]
  :<- [:selected-chat-command]
  :<- [:get-available-commands-responses]
  (fn [[show-suggestions? {:keys [input-text]} selected-command commands-responses]]
    (and (or show-suggestions? (input-model/starts-as-command? (string/trim (or input-text ""))))
         (not (:command selected-command))
         (seq commands-responses))))

(reg-sub
  :is-request-answered?
  :<- [:get-current-chat]
  (fn [{:keys [requests]} [_ message-id]]
    (not= "open" (get-in requests [message-id :status]))))

(reg-sub
  :unviewed-messages-count
  (fn [[_ chat-id]]
    (subscribe [:get-chat chat-id]))
  (fn [{:keys [unviewed-messages]}]
    (count unviewed-messages)))

(reg-sub
  :web-view-extra-js
  :<- [:get-current-chat]
  (fn [current-chat]
    (:web-view-extra-js current-chat)))

(reg-sub
  :get-photo-path
  :<- [:get-contacts]
  (fn [contacts [_ id]]
    (:photo-path (contacts id))))

(reg-sub
  :get-last-message
  (fn [[_ chat-id]]
    (subscribe [:get-chat-message-datemark-groups chat-id]))
  (comp first second first))

(reg-sub
  :get-default-container-area-height
  :<- [:get-current-chat-ui-prop :input-height]
  :<- [:get :layout-height]
  :<- [:chat-input-margin]
  (fn [[input-height layout-height chat-input-margin]]
    (let [bottom (+ input-height chat-input-margin)]
      (input-utils/default-container-area-height bottom layout-height))))

(reg-sub
  :get-max-container-area-height
  :<- [:get-current-chat-ui-prop :input-height]
  :<- [:get :layout-height]
  :<- [:chat-input-margin]
  (fn [[input-height layout-height chat-input-margin]]
    (let [bottom (+ input-height chat-input-margin)]
      (input-utils/max-container-area-height bottom layout-height))))

(reg-sub
  :chat-animations
  (fn [db [_ key type]]
    (let [chat-id (subscribe [:get-current-chat-id])]
      (get-in db [:chat-animations @chat-id key type]))))
