(ns legacy.status-im.chat.models.mentions
  (:require
    [clojure.set :as set]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(defn- transfer-input-segments
  [segments]
  (map (fn [segment]
         (let [{:keys [type value]} segment
               type                 (case type
                                      0 :text
                                      1 :mention
                                      (log/warn "unknown segment type" {:type type}))]
           [type value]))
       segments))

(defn- rename-at-idxs
  [at-idxs]
  (map #(set/rename-keys %
                         {:From      :from
                          :To        :to
                          :Checked   :checked?
                          :Mentioned :mention?
                          :Mention   :mention
                          :NextAtIdx :next-at-idx})
       at-idxs))

(defn- rename-state
  [state]
  (-> state
      (set/rename-keys {:AtSignIdx    :at-sign-idx
                        :AtIdxs       :at-idxs
                        :MentionEnd   :mention-end
                        :PreviousText :previous-text
                        :NewText      :new-text
                        :Start        :start
                        :End          :end})
      (update :at-idxs rename-at-idxs)))

; referenced function: contact-list-item
(defn- rename-mentionable-users
  [mentionable-users]
  (reduce (fn [acc [id v]]
            (assoc acc
                   id
                   (set/rename-keys v
                                    {:id            :public-key
                                     :primaryName   :primary-name
                                     :secondaryName :secondary-name
                                     :compressedKey :compressed-key
                                     :ensVerified   :ens-verified
                                     :added         :added?
                                     :displayName   :display-name
                                     :searchedText  :searched-text})))

          {}
          mentionable-users))

(defn- transfer-mention-result
  [result]
  (let [{:keys [input-segments mentionable-users state chat-id new-text]}
        (set/rename-keys result
                         {:InputSegments      :input-segments
                          :MentionSuggestions :mentionable-users
                          :MentionState       :state
                          :ChatID             :chat-id
                          :NewText            :new-text})]
    {:chat-id           chat-id
     :input-segments    (transfer-input-segments input-segments)
     :mentionable-users (rename-mentionable-users mentionable-users)
     :state             (rename-state state)
     :new-text          new-text}))

(rf/defn on-error
  {:events [:mention/on-error]}
  [_ context error]
  (log/error "[mentions] on-error"
             {:context context
              :error   error}))

(rf/defn to-input-field
  {:events [:mention/to-input-field]}
  [_ text chat-id]
  (let [params [chat-id text]
        method "wakuext_chatMentionToInputField"]
    (log/debug "[mentions] to-input-field" {:params params})
    {:json-rpc/call [{:method     method
                      :params     params
                      :on-success #(rf/dispatch [:mention/on-to-input-field-success %])
                      :on-error   #(rf/dispatch [:mention/on-error
                                                 {:method method
                                                  :params params} %])}]}))
(rf/defn on-to-input-field-success
  {:events [:mention/on-to-input-field-success]}
  [{:keys [db]} result]
  (log/debug "[mentions] on-to-input-field-success" {:result result})
  (let [{:keys [input-segments state chat-id new-text]} (transfer-mention-result result)]
    {:db (-> db
             (assoc-in [:chats/mentions chat-id :mentions] state)
             (assoc-in [:chat/inputs-with-mentions chat-id] input-segments))}))

(rf/defn on-change-text
  {:events [:mention/on-change-text]}
  [{:keys [db]} text]
  (let [chat-id (:current-chat-id db)
        params  [chat-id text]
        method  "wakuext_chatMentionOnChangeText"]
    (log/debug "[mentions] on-change-text" {:params params})
    {:json-rpc/call [{:method     method
                      :params     params
                      :on-success #(rf/dispatch [:mention/on-change-text-success %])
                      :on-error   #(rf/dispatch [:mention/on-error
                                                 {:method method
                                                  :params params} %])}]}))

(rf/defn on-change-text-success
  {:events [:mention/on-change-text-success]}
  [{:keys [db]} result]
  (log/debug "[mentions] on-change-text-success" {:result result})
  (let [{:keys [state chat-id mentionable-users input-segments]} (transfer-mention-result result)]
    {:db (-> db
             (assoc-in [:chats/mention-suggestions chat-id] mentionable-users)
             (assoc-in [:chats/mentions chat-id :mentions] state)
             (assoc-in [:chat/inputs-with-mentions chat-id] input-segments))}))

(rf/defn on-select-mention-success
  {:events [:mention/on-select-mention-success]}
  [{:keys [db] :as cofx} result primary-name match searched-text public-key]
  (log/debug "[mentions] on-select-mention-success"
             {:result        result
              :primary-name  primary-name
              :match         match
              :searched-text searched-text
              :public-key    public-key})
  (let [{:keys [new-text chat-id state input-segments]} (transfer-mention-result result)]
    {:db       (-> db
                   (assoc-in [:chats/mentions chat-id :mentions] state)
                   (assoc-in [:chat/inputs-with-mentions chat-id] input-segments)
                   (assoc-in [:chats/mention-suggestions chat-id] nil))
     :dispatch [:chat.ui/set-chat-input-text new-text chat-id]}))

(rf/defn clear-suggestions
  [{:keys [db]}]
  (log/debug "[mentions] clear suggestions")
  (let [chat-id (:current-chat-id db)]
    {:db (update db :chats/mention-suggestions dissoc chat-id)}))

(rf/defn clear-mentions
  [{:keys [db] :as cofx}]
  (log/debug "[mentions] clear mentions")
  (let [chat-id (:current-chat-id db)]
    (rf/merge
     cofx
     {:db            (-> db
                         (update-in [:chats/mentions chat-id] dissoc :mentions)
                         (update :chat/inputs-with-mentions dissoc chat-id))
      :json-rpc/call [{:method     "wakuext_chatMentionClearMentions"
                       :params     [chat-id]
                       :on-success #()
                       :on-error   #(log/error "Error while calling wakuext_chatMentionClearMentions"
                                               {:error %})}]}
     (clear-suggestions))))

(rf/defn select-mention
  {:events [:chat.ui/select-mention]}
  [{:keys [db]} {:keys [primary-name searched-text match public-key] :as user}]
  (let [chat-id (:current-chat-id db)
        text    (get-in db [:chat/inputs chat-id :input-text])
        method  "wakuext_chatMentionSelectMention"
        params  [chat-id text primary-name public-key]]
    {:json-rpc/call [{:method     method
                      :params     params
                      :on-success #(rf/dispatch [:mention/on-select-mention-success %
                                                 primary-name match searched-text public-key])
                      :on-error   #(rf/dispatch [:mention/on-error
                                                 {:method method
                                                  :params params} %])}]}))
