(ns status-im.chat.models.message
  (:require [re-frame.core :as re-frame]
            [status-im.chat.models :as chat-model]
            [status-im.chat.models.loading :as chat-loading]
            [status-im.chat.models.message-list :as message-list]
            [status-im.constants :as constants]
            [status-im.data-store.messages :as data-store.messages]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.transport.message.protocol :as protocol]
            [status-im.ui.screens.chat.state :as view.state]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [status-im.chat.models.mentions :as mentions]
            [clojure.string :as string]))

(defn- prepare-message
  [message current-chat?]
  (cond-> message
    current-chat?
    (assoc :seen true)))

(fx/defn rebuild-message-list
  [{:keys [db]} chat-id]
  {:db (assoc-in db [:message-lists chat-id]
                 (message-list/add-many nil (vals (get-in db [:messages chat-id]))))})

(fx/defn hidden-message-marked-as-seen
  {:events [::hidden-message-marked-as-seen]}
  [{:keys [db] :as cofx} chat-id _ hidden-message-count]
  (when (= 1 hidden-message-count)
    {:db (update-in db [:chats chat-id]
                    update
                    :unviewed-messages-count dec)}))
(fx/defn hide-message
  "Hide chat message, rebuild message-list"
  [{:keys [db] :as cofx} chat-id {:keys [seen message-id]}]
  (fx/merge cofx
            {:db (update-in db [:messages chat-id] dissoc message-id)}
            (data-store.messages/mark-messages-seen chat-id [message-id] #(re-frame/dispatch [::hidden-message-marked-as-seen %1 %2 %3]))
            (rebuild-message-list chat-id)))

(fx/defn add-message
  [{:keys [db] :as cofx}
   {{:keys [chat-id message-id replace timestamp from] :as message} :message
    :keys [seen-by-user?]}]
  (let [current-public-key (multiaccounts.model/current-public-key cofx)
        message-to-be-removed (when replace
                                (get-in db [:messages chat-id replace]))
        prepared-message (prepare-message message seen-by-user?)]
    (fx/merge cofx
              (when message-to-be-removed
                (hide-message chat-id message-to-be-removed))
              (fn [{:keys [db]}]
                {:db (cond-> (-> db
                                 ;; We should not be always adding to the list, as it does not make sense
                                 ;; if the chat has not been initialized, but run into
                                 ;; some troubles disabling it, so next time
                                 (update-in [:messages chat-id] assoc message-id prepared-message)
                                 (update-in [:message-lists chat-id] message-list/add prepared-message))
                       (and (not seen-by-user?)
                            (not= from current-public-key)
                            (not (get-in db [:chats chat-id :profile-public-key]))
                            (not (get-in db [:chats chat-id :timeline?])))
                       (update-in [:chats chat-id :loaded-unviewed-messages-ids]
                                  (fnil conj #{}) message-id))}))))

(fx/defn add-sender-to-chat-users
  [{:keys [db]} {:keys [chat-id alias name identicon from]}]
  (when (and alias (not= alias ""))
    (let [nickname (get-in db [:contacts/contacts from :nickname])]
      {:db (update-in db [:chats chat-id :users] assoc
                      from
                      (mentions/add-searchable-phrases
                       {:alias      alias
                        :name       (or name alias)
                        :identicon  identicon
                        :public-key from
                        :nickname   nickname}))})))

(fx/defn add-received-message
  [{:keys [db] :as cofx}
   {:keys [chat-id clock-value] :as message}]
  (let [{:keys [view-id current-chat-id]} db
        cursor-clock-value (get-in db [:chats current-chat-id :cursor-clock-value])]
    (fx/merge
     cofx
     ;;TODO we want to add all messages, but we need to optimize that, so we can add messages smooth
     (add-message {:message       message
                   ;;TODO we need to find if this is an active chat
                   :seen-by-user? (= view-id :chat)})
     ;; If we don't have any hidden message or the hidden message is before
     ;; this one, we add the message to the UI
     #_(if (or (not @view.state/first-not-visible-item)
               (<= (:clock-value @view.state/first-not-visible-item)
                   clock-value))
         (add-message {:message       message
                       ;;TODO we need to find if this is an active chat
                       :seen-by-user? (= view-id :chat)})
         ;; Not in the current view, set all-loaded to false
         ;; and offload to db and update cursor if necessary
         {:db (cond-> (assoc-in db [:chats chat-id :all-loaded?] false)
                (>= clock-value cursor-clock-value)
                (update-in [:chats chat-id] assoc
                           :cursor (chat-loading/clock-value->cursor clock-value)
                           :cursor-clock-value clock-value))})
     (add-sender-to-chat-users message))))

(defn- message-loaded?
  [{:keys [db]} {:keys [chat-id message-id]}]
  (get-in db [:messages chat-id message-id]))

(defn- earlier-than-deleted-at?
  [{:keys [db]} {:keys [chat-id clock-value]}]
  (let [{:keys [deleted-at-clock-value]}
        (get-in db [:chats chat-id])]
    (>= deleted-at-clock-value clock-value)))

(fx/defn update-unviewed-count
  [{:keys [db] :as cofx} {:keys [chat-id from message-type message-id new?]}]
  (when-not (= message-type constants/message-type-private-group-system-message)
    (let [{:keys [current-chat-id view-id]} db
          chat-view?         (= :chat view-id)
          current-count (get-in db [:chats chat-id :unviewed-messages-count])]
      (cond
        (= from (multiaccounts.model/current-public-key cofx))
       ;; nothing to do
        nil

        (and chat-view? (= current-chat-id chat-id))
        (fx/merge cofx
                  (data-store.messages/mark-messages-seen current-chat-id [message-id] nil))

        new?
        {:db (update-in db [:chats chat-id]
                        assoc
                        :unviewed-messages-count (inc current-count))}))))

(fx/defn check-for-incoming-tx
  [cofx {{:keys [transaction-hash]} :command-parameters}]
  (when (and transaction-hash
             (not (string/blank? transaction-hash)))
    ;; NOTE(rasom): dispatch later is needed because of circular dependency
    {:dispatch-later
     [{:dispatch [:watch-tx transaction-hash]
       :ms       20}]}))

(fx/defn receive-one
  {:events [::receive-one]}
  [{:keys [db] :as cofx} {:keys [message-id chat-id] :as message}]
  (fx/merge cofx
            ;; TODO this is not cool
            ;;If its a profile updates we want to add this message to the timeline as well
            #(when (get-in cofx [:db :chats chat-id :profile-public-key])
               {:dispatch-n [[::receive-one (assoc message :chat-id chat-model/timeline-chat-id)]]})

            #(when-not (earlier-than-deleted-at? cofx message)
               (if (message-loaded? cofx message)
                 ;; If the message is already loaded, it means it's an update, that
                 ;; happens when a message that was missing a reply had the reply
                 ;; coming through, in which case we just insert the new message
                 {:db (assoc-in db [:messages chat-id message-id] message)}
                 (fx/merge cofx
                           (add-received-message message)
                           (update-unviewed-count message)
                           (chat-model/join-time-messages-checked chat-id)
                           (check-for-incoming-tx message))))))

;;;; Send message
(fx/defn update-message-status
  [{:keys [db] :as cofx} chat-id message-id status]
  (fx/merge cofx
            {:db (assoc-in db
                           [:messages chat-id message-id :outgoing-status]
                           status)}
            (data-store.messages/update-outgoing-status message-id status)))

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
  [{:keys [db] :as cofx} chat-id message-id]
  (fx/merge cofx
            {:db            (update-in db [:messages chat-id] dissoc message-id)}
            (data-store.messages/delete-message message-id)
            (rebuild-message-list chat-id)))

(fx/defn send-message
  [{:keys [db now] :as cofx} message]
  (protocol/send-chat-messages cofx [message]))

(fx/defn send-messages
  [{:keys [db now] :as cofx} messages]
  (protocol/send-chat-messages cofx messages))

(fx/defn toggle-expand-message
  [{:keys [db]} chat-id message-id]
  {:db (update-in db [:messages chat-id message-id :expanded?] not)})
