(ns status-im.chat.models
  (:require [status-im.data-store.chats :as chats-store]
            [status-im.data-store.messages :as messages-store]
            [status-im.transport.message.core :as transport.message]
            [status-im.transport.message.v1.group-chat :as transport.group-chat]
            [status-im.transport.utils :as transport.utils]
            [status-im.ui.components.styles :as styles]
            [status-im.utils.clocks :as utils.clocks]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.platform :as platform]))

(defn multi-user-chat? [chat-id cofx]
  (get-in cofx [:db :chats chat-id :group-chat]))

(defn group-chat? [chat-id cofx]
  (and (multi-user-chat? chat-id cofx)
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

(defn upsert-chat
  "Upsert chat when not deleted"
  [{:keys [chat-id] :as chat-props} {:keys [db] :as cofx}]
  (let [chat (merge
              (or (get (:chats db) chat-id)
                  (create-new-chat chat-id cofx))
              chat-props)]

    (if (:is-active chat)
      {:db            (update-in db [:chats chat-id] merge chat)
       :data-store/tx [(chats-store/save-chat-tx chat)]}
      ;; when chat is deleted, don't change anything
      {:db db})))

(defn add-public-chat
  "Adds new public group chat to db & realm"
  [topic cofx]
  (upsert-chat {:chat-id          topic
                :is-active        true
                :name             topic
                :group-chat       true
                :contacts         []
                :public?          true} cofx))

(defn add-group-chat
  "Adds new private group chat to db & realm"
  [chat-id chat-name admin participants cofx]
  (upsert-chat {:chat-id     chat-id
                :name        chat-name
                :is-active   true
                :group-chat  true
                :group-admin admin
                :contacts    participants} cofx))

(defn clear-history [chat-id {:keys [db] :as cofx}]
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

(defn- remove-transport [chat-id {:keys [db] :as cofx}]
  ;; if this is private group chat, we have to broadcast leave and unsubscribe after that
  (if (group-chat? chat-id cofx)
    (transport.message/send (transport.group-chat/GroupLeave.) chat-id cofx)
    (transport.utils/unsubscribe-from-chat chat-id cofx)))

(defn- deactivate-chat [chat-id {:keys [db now] :as cofx}]
  (cond-> (assoc-in {:db db
                     :data-store/tx [(chats-store/deactivate-chat-tx chat-id now)]}
                    [:db :chats chat-id :is-active] false)
    platform/desktop?
    (assoc-in [:db :current-chat-id] nil)))

;; TODO: There's a race condition here, as the removal of the filter (async)
;; is done at the same time as the removal of the chat, so a message
;; might come between and restore the chat. Multiple way to handle this
;; (remove chat only after the filter has been removed, probably the safest,
;; flag the chat to ignore new messages, change receive method for public/group chats)
;; For now to keep the code simplier and avoid significant changes, best to leave as it is.
(defn remove-chat [chat-id {:keys [db now] :as cofx}]
  (letfn [(remove-transport-fx [chat-id cofx]
            (when (multi-user-chat? chat-id cofx)
              (remove-transport chat-id cofx)))]
    (handlers-macro/merge-fx
     cofx
     (remove-transport-fx chat-id)
     (deactivate-chat chat-id)
     (clear-history chat-id))))

(defn bot-only-chat? [db chat-id]
  (let [{:keys [group-chat contacts]} (get-in db [:chats chat-id])]
    (and (not group-chat)
         (get-in db [:contacts/contacts (first contacts) :dapp?]))))
