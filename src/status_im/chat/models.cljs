(ns status-im.chat.models
  (:require [status-im.data-store.chats :as chats-store]
            [status-im.data-store.messages :as messages-store]
            [status-im.data-store.user-statuses :as user-statuses-store]
            [status-im.transport.message.core :as transport.message]
            [status-im.transport.message.v1.protocol :as protocol]
            [status-im.transport.message.v1.group-chat :as transport.group-chat]
            [status-im.transport.utils :as transport.utils]
            [status-im.ui.components.styles :as styles]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.clocks :as utils.clocks]
            [status-im.utils.datetime :as time]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.fx :as fx]))

(defn multi-user-chat? [cofx chat-id]
  (get-in cofx [:db :chats chat-id :group-chat]))

(defn group-chat? [cofx chat-id]
  (and (multi-user-chat? cofx chat-id)
       (not (get-in cofx [:db :chats chat-id :public?]))))

(defn public-chat? [chat-id cofx]
  (get-in cofx [:db :chats chat-id :public?]))

(defn set-chat-ui-props
  "Updates ui-props in active chat by merging provided kvs into them"
  [{:keys [current-chat-id] :as db} kvs]
  (update-in db [:chat-ui-props current-chat-id] merge kvs))

(defn toggle-chat-ui-prop
  "Toggles chat ui prop in active chat"
  [{:keys [current-chat-id] :as db} ui-element]
  (update-in db [:chat-ui-props current-chat-id ui-element] not))

(defn- create-new-chat
  [chat-id {:keys [db now]}]
  (let [name (get-in db [:contacts/contacts chat-id :name])]
    {:chat-id            chat-id
     :name               (or name (gfycat/generate-gfy chat-id))
     :color              (styles/random-chat-color)
     :group-chat         false
     :is-active          true
     :timestamp          now
     :contacts           [chat-id]
     :last-clock-value   0}))

(fx/defn upsert-chat
  "Upsert chat when not deleted"
  [{:keys [db] :as cofx} {:keys [chat-id] :as chat-props}]
  (let [chat (merge
              (or (get (:chats db) chat-id)
                  (create-new-chat chat-id cofx))
              chat-props)]

    (if (:is-active chat)
      {:db            (update-in db [:chats chat-id] merge chat)
       :data-store/tx [(chats-store/save-chat-tx chat)]}
      ;; when chat is deleted, don't change anything
      {:db db})))

(fx/defn add-public-chat
  "Adds new public group chat to db & realm"
  [cofx topic]
  (upsert-chat cofx
               {:chat-id          topic
                :is-active        true
                :name             topic
                :group-chat       true
                :contacts         []
                :public?          true}))

(fx/defn add-group-chat
  "Adds new private group chat to db & realm"
  [cofx chat-id chat-name admin participants]
  (upsert-chat cofx
               {:chat-id     chat-id
                :name        chat-name
                :is-active   true
                :group-chat  true
                :group-admin admin
                :contacts    participants}))

(fx/defn clear-history
  "Clears history of the particular chat"
  [{:keys [db] :as cofx} chat-id]
  (let [{:keys [messages
                deleted-at-clock-value]} (get-in db [:chats chat-id])
        last-message-clock-value (or (->> messages
                                          vals
                                          (sort-by (comp unchecked-negate :clock-value))
                                          first
                                          :clock-value)
                                     deleted-at-clock-value
                                     (utils.clocks/send 0))]
    {:db            (update-in db [:chats chat-id] merge
                               {:messages               {}
                                :message-groups         {}
                                :unviewed-messages      #{}
                                :not-loaded-message-ids #{}
                                :deleted-at-clock-value last-message-clock-value})
     :data-store/tx [(chats-store/clear-history-tx chat-id last-message-clock-value)
                     (messages-store/delete-messages-tx chat-id)]}))

(fx/defn remove-transport
  [{:keys [db] :as cofx} chat-id]
  ;; if this is private group chat, we have to broadcast leave and unsubscribe after that
  (if (group-chat? cofx chat-id)
    (transport.message/send (transport.group-chat/GroupLeave.) chat-id cofx)
    (transport.utils/unsubscribe-from-chat cofx chat-id)))

(fx/defn deactivate-chat
  [{:keys [db now] :as cofx} chat-id]
  {:db (-> db
           (assoc-in [:chats chat-id :is-active] false)
           (assoc-in [:current-chat-id] nil))
   :data-store/tx [(chats-store/deactivate-chat-tx chat-id now)]})

;; TODO: There's a race condition here, as the removal of the filter (async)
;; is done at the same time as the removal of the chat, so a message
;; might come between and restore the chat. Multiple way to handle this
;; (remove chat only after the filter has been removed, probably the safest,
;; flag the chat to ignore new messages, change receive method for public/group chats)
;; For now to keep the code simplier and avoid significant changes, best to leave as it is.
(fx/defn remove-chat
  "Removes chat completely from app, producing all necessary effects for that"
  [{:keys [db now] :as cofx} chat-id]
  (fx/merge cofx
            #(when (multi-user-chat? % chat-id)
               (remove-transport % chat-id))
            (deactivate-chat chat-id)
            (clear-history chat-id)))

(fx/defn send-messages-seen
  [{:keys [db] :as cofx} chat-id message-ids]
  (when (not (get-in db [:chats chat-id :public?]))
    (transport.message/send (protocol/map->MessagesSeen {:message-ids message-ids}) chat-id cofx)))

;; TODO (janherich) - ressurect `constants/system` messages for group chats in the future
(fx/defn mark-messages-seen
  "Marks all unviewed loaded messages as seen in particular chat"
  [{:keys [db] :as cofx} chat-id]
  (when-let [all-unviewed-ids (seq (get-in db [:chats chat-id :unviewed-messages]))]
    (let [me                  (:current-public-key db)
          updated-statuses    (keep (fn [message-id]
                                      (some-> db
                                              (get-in [:chats chat-id :message-statuses
                                                       message-id me])
                                              (assoc :status :seen)))
                                    all-unviewed-ids)
          loaded-unviewed-ids (map :message-id updated-statuses)]
      (when (seq loaded-unviewed-ids)
        (fx/merge cofx
                  {:db (-> (reduce (fn [acc {:keys [message-id status]}]
                                     (assoc-in acc [:chats chat-id :message-statuses
                                                    message-id me :status]
                                               status))
                                   db
                                   updated-statuses)
                           (update-in [:chats chat-id :unviewed-messages]
                                      #(apply disj % loaded-unviewed-ids)))
                   :data-store/tx [(user-statuses-store/save-statuses-tx updated-statuses)]}
                  (send-messages-seen chat-id loaded-unviewed-ids))))))

(fx/defn preload-chat-data
  "Takes chat-id and coeffects map, returns effects necessary when navigating to chat"
  [{:keys [db] :as cofx} chat-id]
  (fx/merge cofx
            {:db (-> (assoc db :current-chat-id chat-id)
                     (set-chat-ui-props {:validation-messages nil}))}
            (mark-messages-seen chat-id)))

(fx/defn navigate-to-chat
  "Takes coeffects map and chat-id, returns effects necessary for navigation and preloading data"
  [cofx chat-id {:keys [modal? navigation-replace?]}]
  (cond
    modal?
    (fx/merge cofx
              (navigation/navigate-to-cofx :chat-modal {})
              (preload-chat-data chat-id))

    navigation-replace?
    (fx/merge cofx
              (navigation/navigate-reset {:index   1
                                          :actions [{:routeName :home}
                                                    {:routeName :chat}]})
              (preload-chat-data chat-id))

    :else
    (fx/merge cofx
              (navigation/navigate-to-cofx :chat {})
              (preload-chat-data chat-id))))

(fx/defn start-chat
  "Start a chat, making sure it exists"
  [{:keys [db] :as cofx} chat-id opts]
  ;; don't allow to open chat with yourself
  (when (not= (:current-public-key db) chat-id)
    (fx/merge cofx
              (upsert-chat {:chat-id chat-id
                            :is-active true})
              (navigate-to-chat chat-id opts))))
