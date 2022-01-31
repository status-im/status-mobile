(ns status-im.chat.models.message
  (:require [status-im.chat.models :as chat-model]
            [re-frame.core :as re-frame]
            [status-im.chat.models.message-list :as message-list]
            [status-im.constants :as constants]
            [status-im.data-store.messages :as data-store.messages]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.transport.message.protocol :as protocol]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [status-im.chat.models.mentions :as mentions]
            [clojure.string :as string]
            [status-im.utils.types :as types]
            [status-im.ui.screens.chat.state :as view.state]
            [status-im.chat.models.loading :as chat.loading]
            [status-im.utils.platform :as platform]
            [status-im.utils.gfycat.core :as gfycat]))

(defn- message-loaded?
  [db chat-id message-id]
  (get-in db [:messages chat-id message-id]))

(defn- earlier-than-deleted-at?
  [db chat-id clock-value]
  (>= (get-in db [:chats chat-id :deleted-at-clock-value]) clock-value))

(defn add-timeline-message [acc chat-id message-id message]
  (-> acc
      (update-in [:db :messages chat-id] assoc message-id message)
      (update-in [:db :message-lists chat-id] message-list/add message)))

(defn hide-message
  "Hide chat message, rebuild message-list"
  [{:keys [db]} chat-id message-id]
  ;; TODO this is too expensive, probably we could mark message somehow and just hide it in the UI
  (message-list/rebuild-message-list {:db (update-in db [:messages chat-id] dissoc message-id)} chat-id))

(fx/defn add-senders-to-chat-users
  {:events [:chat/add-senders-to-chat-users]}
  [{:keys [db]} messages]
  (reduce (fn [acc {:keys [chat-id alias name identicon from]}]
            (let [alias (if (string/blank? alias)
                          (gfycat/generate-gfy from)
                          alias)]
              (update-in acc [:db :chats chat-id :users] assoc
                         from
                         (mentions/add-searchable-phrases
                          {:alias      alias
                           :name       (or name alias)
                           :identicon  identicon
                           :public-key from
                           :nickname   (get-in db [:contacts/contacts from :nickname])}))))
          {:db db}
          messages))

(defn timeline-message? [db chat-id]
  (and
   (get-in db [:pagination-info constants/timeline-chat-id :messages-initialized?])
   (or
    (= chat-id (chat-model/my-profile-chat-topic db))
    (when-let [pub-key (get-in db [:chats chat-id :profile-public-key])]
      (get-in db [:contacts/contacts pub-key :added])))))

(defn get-timeline-message [db chat-id message-js]
  (when (timeline-message? db chat-id)
    (data-store.messages/<-rpc (types/js->clj message-js))))

(defn add-message [{:keys [db] :as acc} message-js chat-id message-id cursor-clock-value]
  (let [{:keys [replace from clock-value] :as message}
        (data-store.messages/<-rpc (types/js->clj message-js))]
    (if (message-loaded? db chat-id message-id)
      ;; If the message is already loaded, it means it's an update, that
      ;; happens when a message that was missing a reply had the reply
      ;; coming through, in which case we just insert the new message
      (assoc-in acc [:db :messages chat-id message-id] message)
      (cond-> acc
        ;;add new message to db
        :always
        (update-in [:db :messages chat-id] assoc message-id message)
        :always
        (update-in [:db :message-lists chat-id] message-list/add message)

        (or (not cursor-clock-value) (< clock-value cursor-clock-value))
        (update-in [:db :pagination-info chat-id] assoc
                   :cursor (chat.loading/clock-value->cursor clock-value)
                   :cursor-clock-value clock-value)

        ;;conj sender for add-sender-to-chat-users
        (not (get-in db [:chats chat-id :users from]))
        (update :senders assoc from message)

        (not (string/blank? replace))
        ;;TODO this is expensive
        (hide-message chat-id replace)))))

(defn reduce-js-messages [{:keys [db] :as acc} ^js message-js]
  (let [chat-id (.-localChatId message-js)
        clock-value (.-clock message-js)
        message-id (.-id message-js)
        current-chat-id (:current-chat-id db)
        cursor-clock-value (get-in db [:pagination-info current-chat-id :cursor-clock-value])]
    ;;ignore not opened chats and earlier clock
    (if (and (get-in db [:pagination-info chat-id :messages-initialized?])
             ;;TODO why do we need this ?
             (not (earlier-than-deleted-at? db chat-id clock-value)))
      (if (or (not @view.state/first-not-visible-item)
              (<= (:clock-value @view.state/first-not-visible-item)
                  clock-value))
        (add-message acc message-js chat-id message-id cursor-clock-value)
        ;; Not in the current view, set all-loaded to false
        ;; and offload to db and update cursor if necessary
        ;;TODO if we'll offload messages , it will conflict with end reached, so probably if we reached the end of visible area,
        ;; we need to drop other messages with (< clock-value cursor-clock-value) from response-js so we don't update
        ;; :cursor-clock-value because it will be changed when we loadMore message
        {:db (cond-> (assoc-in db [:pagination-info chat-id :all-loaded?] false)
               (> clock-value cursor-clock-value)
               ;;TODO cut older messages from messages-list
               (update-in [:pagination-info chat-id] assoc
                          :cursor (chat.loading/clock-value->cursor clock-value)
                          :cursor-clock-value clock-value))})
      acc)))

(defn receive-many [{:keys [db]} ^js response-js]
  (let [messages-js ^js (.splice (.-messages response-js) 0 (if platform/low-device? 3 10))
        {:keys [db senders]}
        (reduce reduce-js-messages
                {:db db :chats #{} :senders {} :transactions #{}}
                messages-js)]
    ;;we want to render new messages as soon as possible
    ;;so we dispatch later all other events which can be handled async
    {:db db
     :utils/dispatch-later
     (concat [{:ms 20 :dispatch [:process-response response-js]}]
             (when (and (:current-chat-id db) (= "active" (:app-state db)))
               [{:ms 100 :dispatch [:chat/mark-all-as-read (:current-chat-id db)]}])
             (when (seq senders)
               [{:ms 100 :dispatch [:chat/add-senders-to-chat-users (vals senders)]}]))}))

(defn reduce-js-statuses [db ^js message-js]
  (let [chat-id (.-localChatId message-js)
        profile-initialized (get-in db [:pagination-info chat-id :messages-initialized?])
        timeline-message (timeline-message? db chat-id)]
    (if (or profile-initialized timeline-message)
      (let [{:keys [message-id] :as message} (data-store.messages/<-rpc (types/js->clj message-js))]
        (cond-> db
          profile-initialized
          (update-in [:messages chat-id] assoc message-id message)
          profile-initialized
          (update-in [:message-lists chat-id] message-list/add message)
          timeline-message
          (update-in [:messages constants/timeline-chat-id] assoc message-id message)
          timeline-message
          (update-in [:message-lists constants/timeline-chat-id] message-list/add message)))
      db)))

(fx/defn process-statuses
  {:events [:process-statuses]}
  [{:keys [db]} statuses]
  {:db (reduce reduce-js-statuses db statuses)})

(fx/defn update-db-message-status
  [{:keys [db] :as cofx} chat-id message-id status]
  (when (get-in db [:messages chat-id message-id])
    (fx/merge cofx
              {:db (assoc-in db
                             [:messages chat-id message-id :outgoing-status]
                             status)})))

(fx/defn update-message-status
  [{:keys [db] :as cofx} chat-id message-id status]
  (fx/merge cofx
            (update-db-message-status chat-id message-id status)))

(fx/defn resend-message
  [{:keys [db] :as cofx} chat-id message-id]
  (fx/merge cofx
            {::json-rpc/call [{:method (json-rpc/call-ext-method "reSendChatMessage")
                               :params [message-id]
                               :on-success #(log/debug "re-sent message successfully")
                               :on-error #(log/error "failed to re-send message" %)}]}
            (update-message-status chat-id message-id :sending)))

(fx/defn delete-message
  "Deletes chat message, rebuild message-list"
  {:events [:chat.ui/delete-message]}
  [{:keys [db] :as cofx} chat-id message-id]
  (fx/merge cofx
            {:db            (update-in db [:messages chat-id] dissoc message-id)}
            (data-store.messages/delete-message message-id)
            (message-list/rebuild-message-list chat-id)))

(fx/defn send-message
  [cofx message]
  (protocol/send-chat-messages cofx [message]))

(fx/defn send-messages
  [cofx messages]
  (protocol/send-chat-messages cofx messages))

(fx/defn handle-removed-messages
  {:events [::handle-removed-messages]}
  [{:keys [db] :as cofx} removed-messages]
  (let [mark-as-seen-fx (mapv (fn [removed-message]
                                (let [chat-id (:chatId removed-message)
                                      message-id (:messageId removed-message)]
                                  (data-store.messages/mark-messages-seen chat-id
                                                                          [message-id]
                                                                          #(re-frame/dispatch [:chat/decrease-unviewed-count chat-id %3])))) removed-messages)
        remove-messages-fx (fn [{:keys [db]}]
                             {:db (reduce (fn [acc current]
                                            (update-in acc [:messages (:chatId current)] dissoc (:messageId current)))
                                          db removed-messages)
                              :dispatch-n [[:get-activity-center-notifications]
                                           [:get-activity-center-notifications-count]]})]
    (apply fx/merge cofx (conj mark-as-seen-fx remove-messages-fx))))

(comment
  (handle-removed-messages
   {:db {:messages {:c1 {:m1 {:chat-id :c1 :message-id :m1}
                         :m2 {:chat-id :c1 :message-id :m2}}
                    :c2 {:m3 {:chat-id :c2 :message-id :m3}
                         :m4 {:chat-id :c2 :message-id :m4}}}}}
   [:m1 :m3]))

(defn remove-cleared-message [messages cleared-at]
  (into {} (remove #(let [message-clock (:clock-value (second %))]
                      (<= message-clock cleared-at))
                   messages)))

(fx/defn handle-cleared-histories-messages
  {:events [::handle-cleared-hisotories-messages]}
  [{:keys [db]} cleared-histories]
  {:db (reduce (fn [acc current]
                 (update-in acc [:messages (:chatId current)] remove-cleared-message (:clearedAt current)))
               db
               cleared-histories)})