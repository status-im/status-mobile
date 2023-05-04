(ns status-im.chat.models.mentions
  (:require [clojure.set :as set]
            [quo.react :as react]
            [quo.react-native :as rn]
            [re-frame.core :as re-frame]
            [status-im2.config :as config]
            [utils.re-frame :as rf]
            [taoensso.timbre :as log]
            [native-module.core :as native-module]))

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
  (reduce (fn [acc [id val]]
            (assoc acc
                   id
                   (set/rename-keys val
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
    {:set-text-input-value [chat-id new-text]
     :db                   (-> db
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
  (log/debug "[mentions] on-text-input-success" {:result result})
  (let [{:keys [state chat-id mentionable-users input-segments]} (transfer-mention-result result)]
    {:db (-> db
             (assoc-in [:chats/mention-suggestions chat-id] mentionable-users)
             (assoc-in [:chats/mentions chat-id :mentions] state)
             (assoc-in [:chat/inputs-with-mentions chat-id] input-segments))}))

(rf/defn recheck-at-idxs
  [{:keys [db]} public-key]
  (let [chat-id (:current-chat-id db)
        text    (get-in db [:chat/inputs chat-id :input-text])
        params  [chat-id text public-key]
        method  "wakuext_chatMentionRecheckAtIdxs"]
    {:json-rpc/call [{:method     method
                      :params     params
                      :on-success #(rf/dispatch [:mention/on-recheck-at-idxs-success %])
                      :on-error   #(rf/dispatch [:mention/on-error
                                                 {:method method
                                                  :params params} %])}]}))

(rf/defn on-recheck-at-idxs-success
  {:events [:mention/on-recheck-at-idxs-success]}
  [{:keys [db]} result]
  (log/debug "[mentions] on-recheck-at-idxs-success" {:result result})
  (let [{:keys [input-segments state chat-id]} (transfer-mention-result result)]
    {:db (-> db
             (assoc-in [:chats/mentions chat-id :mentions] state)
             (assoc-in [:chat/inputs-with-mentions chat-id] input-segments))}))

(rf/defn reset-text-input-cursor
  [_ ref cursor]
  {::reset-text-input-cursor [ref cursor]})

(rf/defn on-new-input-text-with-mentions-success
  {:events [:mention/on-new-input-text-with-mentions-success]}
  [{:keys [db] :as cofx} result primary-name text-input-ref match searched-text public-key]
  (log/debug "[mentions] on-new-input-text-with-mentions-success"
             {:result        result
              :primary-name  primary-name
              :match         match
              :searched-text searched-text
              :public-key    public-key})
  (let [{:keys [new-text state chat-id]} (transfer-mention-result result)
        {:keys [at-sign-idx]}            state
        cursor                           (+ at-sign-idx (count primary-name) 2)]
    (rf/merge
     cofx
     (let [common {:db       (-> db
                                 (assoc-in [:chats/mention-suggestions chat-id] nil))
                   :dispatch [:chat.ui/set-chat-input-text new-text chat-id]}
           extra  (if (not config/new-composer-enabled?)
                    ;; NOTE(rasom): Some keyboards do not react on selection property passed to
                    ;; text input (specifically Samsung keyboard with predictive text set on).
                    ;; In this case, if the user continues typing after the programmatic change,
                    ;; the new text is added to the last known cursor position before
                    ;; programmatic change. By calling `reset-text-input-cursor` we force the
                    ;; keyboard's cursor position to be changed before the next input.
                    {:set-text-input-value    [chat-id new-text text-input-ref]
                     :reset-text-input-cursor (reset-text-input-cursor text-input-ref cursor)}
                    {})]
       (merge common extra))
     (recheck-at-idxs public-key))))

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

(rf/defn check-selection
  {:events [:mention/on-selection-change]}
  [{:keys [db]}
   {:keys [start end]}]
  (let [chat-id (:current-chat-id db)
        text    (get-in db [:chat/inputs chat-id :input-text])
        params  [chat-id text start end]
        method  "wakuext_chatMentionHandleSelectionChange"]
    (when text
      (log/debug "[mentions] check-selection" {:params params})
      {:json-rpc/call [{:method     method
                        :params     params
                        :on-success #(rf/dispatch [:mention/on-handle-selection-change-success %])
                        :on-error   #(rf/dispatch [:mention/on-error
                                                   {:method method
                                                    :params params} %])}]})))

(rf/defn on-check-selection-success
  {:events [:mention/on-handle-selection-change-success]}
  [{:keys [db]} result]
  (log/debug "[mentions] on-check-selection-success" {:result result})
  (let [{:keys [state chat-id mentionable-users input-segments]} (transfer-mention-result result)]
    {:db (-> db
             (assoc-in [:chats/mention-suggestions chat-id] mentionable-users)
             (assoc-in [:chats/mentions chat-id :mentions] state)
             (assoc-in [:chat/inputs-with-mentions chat-id] input-segments))}))

(re-frame/reg-fx
 ::reset-text-input-cursor
 (fn [[ref cursor]]
   (when ref
     (native-module/reset-keyboard-input
      (rn/find-node-handle (react/current-ref ref))
      cursor))))
