(ns status-im.chat.models.mentions
  (:require [clojure.set :as set]
            [quo.react :as react]
            [quo.react-native :as rn]
            [re-frame.core :as re-frame]
            [utils.re-frame :as rf]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]
            [status-im.native-module.core :as status]))

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
                                     :searchedText  :searched-text
                                    })))
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
(rf/defn on-to-input-field-success
  {:events [:mention/on-to-input-field-success]}
  [{:keys [db]} result]
  (log/debug "[mentions] on-to-input-field-success" {:result result})
  (let [{:keys [input-segments state chat-id new-text]} (transfer-mention-result result)]
    {:set-text-input-value [chat-id new-text]
     :db                   (-> db
                               (assoc-in [:chats/mentions chat-id :mentions] state)
                               (assoc-in [:chat/inputs-with-mentions chat-id] input-segments))}))

(rf/defn on-text-input
  {:events [:mention/on-text-input]}
  [{:keys [db]} {:keys [previous-text start end] :as args}]
  (let [previous-text
        ;; NOTE(rasom): on iOS `previous-text` contains entire input's text. To
        ;; get only removed part of text we have cut it.
        (if platform/android?
          previous-text
          (subs previous-text start end))
        chat-id       (:current-chat-id db)
        state         (merge args {:previous-text previous-text})
        state         (set/rename-keys state
                                       {:previous-text :PreviousText
                                        :new-text      :NewText
                                        :start         :Start
                                        :end           :End})
        params        [chat-id state]
        method        "wakuext_chatMentionOnTextInput"]
    (log/debug "[mentions] on-text-input" {:params params})
    {:json-rpc/call [{:method     method
                      :params     [chat-id state]
                      :on-success #(rf/dispatch [:mention/on-text-input-success %])
                      :on-error   #(rf/dispatch [:mention/on-error
                                                 {:method method
                                                  :params params} %])}]}))

(rf/defn on-text-input-success
  {:events [:mention/on-text-input-success]}
  [{:keys [db]} result]
  (log/debug "[mentions] on-text-input-success" {:result result})
  (let [{:keys [state chat-id]} (transfer-mention-result result)]
    {:db (assoc-in db [:chats/mentions chat-id :mentions] state)}))

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
     {:db                   (-> db
                                (assoc-in [:chats/mention-suggestions chat-id] nil))
      :set-text-input-value [chat-id new-text text-input-ref]
      :dispatch             [:chat.ui/set-chat-input-text new-text chat-id]}
     ;; NOTE(rasom): Some keyboards do not react on selection property passed to
     ;; text input (specifically Samsung keyboard with predictive text set on).
     ;; In this case, if the user continues typing after the programmatic change,
     ;; the new text is added to the last known cursor position before
     ;; programmatic change. By calling `reset-text-input-cursor` we force the
     ;; keyboard's cursor position to be changed before the next input.
     (reset-text-input-cursor text-input-ref cursor)
     ;; NOTE(roman): on-text-input event is not dispatched when we change input
     ;; programmatically, so we have to call `on-text-input` manually
     (on-text-input
      (let [match-len (count match)
            start     (inc at-sign-idx)
            end       (+ start match-len)]
        {:new-text      match
         :previous-text searched-text
         :start         start
         :end           end}))
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
                        :params     [chat-id text start end]
                        :on-success #(rf/dispatch [:mention/on-handle-selection-change-success %])
                        :on-error   #(rf/dispatch [:mention/on-error
                                                   {:method method
                                                    :params params} %])}]})))

(rf/defn on-check-selection-success
  {:events [:mention/on-handle-selection-change-success]}
  [{:keys [db]} result]
  (log/debug "[mentions] on-check-selection-success" {:result result})
  (let [{:keys [state chat-id]} (transfer-mention-result result)]
    {:db (assoc-in db [:chats/mentions chat-id :mentions] (rename-state state))}))

(re-frame/reg-fx
 ::reset-text-input-cursor
 (fn [[ref cursor]]
   (when ref
     (status/reset-keyboard-input
      (rn/find-node-handle (react/current-ref ref))
      cursor))))

(rf/defn calculate-suggestions
  {:events [:mention/calculate-suggestions]}
  [{:keys [db]}]
  (let [chat-id (:current-chat-id db)
        text    (get-in db [:chat/inputs chat-id :input-text])
        params  [chat-id text]
        method  "wakuext_chatMentionCalculateSuggestions"]
    (log/debug "[mentions] calculate-suggestions" {:params params})
    {:json-rpc/call [{:method     method
                      :params     [chat-id text]
                      :on-success #(rf/dispatch [:mention/on-calculate-suggestions-success %])
                      :on-error   #(rf/dispatch [:mention/on-error
                                                 {:method method
                                                  :params params} %])}]}))

(rf/defn on-calculate-suggestions-success
  {:events [:mention/on-calculate-suggestions-success]}
  [{:keys [db]} result]
  (log/debug "[mentions] on-calculate-suggestions-success" {:result result})
  (let [{:keys [state chat-id mentionable-users input-segments]} (transfer-mention-result result)]
    {:db (-> db
             (assoc-in [:chats/mention-suggestions chat-id] mentionable-users)
             (assoc-in [:chats/mentions chat-id :mentions] state)
             (assoc-in [:chat/inputs-with-mentions chat-id] input-segments))}))
