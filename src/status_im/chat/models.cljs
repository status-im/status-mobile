(ns status-im.chat.models
  (:require [status-im.ui.components.styles :as styles]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.handlers :as handlers]
            [status-im.transport.core :as transport]))

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
    {:chat-id               chat-id
     :name                  (or name (gfycat/generate-gfy chat-id))
     :color                 styles/default-chat-color
     :group-chat            false
     :is-active             true
     :timestamp             now
     :contacts              [{:identity chat-id}]
     :last-from-clock-value 0
     :last-to-clock-value   0}))

(defn add-chat
  "Adds new chat to db & realm, if the chat with same id already exists, justs restores it"
  ([chat-id cofx]
   (add-chat chat-id {} cofx))
  ([chat-id chat-props {:keys [db get-stored-chat] :as cofx}]
   (let [{:keys [deleted-chats]} db
         new-chat (merge (if (get deleted-chats chat-id)
                           (assoc (get-stored-chat chat-id) :is-active true)
                           (create-new-chat chat-id cofx))
                         chat-props)]
     {:db        (-> db
                     (update :chats assoc chat-id new-chat)
                     (update :deleted-chats (fnil disj #{}) chat-id))
      :data-store/save-chat new-chat})))

(defn add-public-chat
  "Adds new public group chat to db & realm"
  [topic {:keys [db now] :as cofx}]
  (let [chat {:chat-id               topic
              :name                  topic
              :color                 styles/default-chat-color
              :group-chat            true
              :public?               true
              :is-active             true
              :timestamp             now
              :last-to-clock-value   0
              :last-from-clock-value 0}]
    {:db        (assoc-in db [:chats topic] chat)
     :data-store/save-chat chat}))

(defn add-group-chat
  "Adds new private group chat to db & realm"
  [chat-id chat-name admin participants {:keys [db now] :as cofx}]
  (let [chat {:chat-id               chat-id
              :name                  chat-name
              :color                 styles/default-chat-color
              :group-chat            true
              :group-admin           admin
              :is-active             true
              :timestamp             now
              :contacts              (mapv (partial hash-map :identity) participants)
              :last-to-clock-value   0
              :last-from-clock-value 0}]
    {:db        (assoc-in db [:chats chat-id] chat)
     :data-store/save-chat chat}))

;; TODO (yenda): there should be an option to update the timestamp
;; this shouldn't need a specific function like `upsert-chat` which
;; is wrongfuly named
(defn update-chat
  "Updates chat properties when not deleted, if chat is not present in app-db, creates a default new one"
  [{:keys [chat-id] :as chat-props} {:keys [db] :as cofx}]
  (let [{:keys [chats deleted-chats]} db]
    (if (get deleted-chats chat-id) ;; when chat is deleted, don't change anything
      {:db db}
      (let [chat (merge (or (get chats chat-id)
                            (create-new-chat chat-id cofx))
                        chat-props)]
        {:db        (update-in db [:chats chat-id] merge chat)
         :data-store/save-chat chat}))))

;; TODO (yenda): an upsert is suppose to add the entry if it doesn't
;; exist and update it if it does
(defn upsert-chat
  "Just like `update-chat` only implicitely updates timestamp"
  [chat cofx]
  (update-chat (assoc chat :timestamp (:now cofx)) cofx))

(defn new-update? [{:keys [added-to-at removed-at removed-from-at]} timestamp]
  (and (> timestamp added-to-at)
       (> timestamp removed-at)
       (> timestamp removed-from-at)))

(defn remove-chat [chat-id {:keys [db] :as cofx}]
  (let [{:keys [chat-id group-chat debug?]} (get-in db [:chats chat-id])
        fx (cond-> {:db (-> db
                            (update :chats dissoc chat-id)
                            (update :deleted-chats (fnil conj #{}) chat-id))}
             (or group-chat debug?)
             (assoc :data-store/delete-messages chat-id)
             debug?
             (assoc :data-store/delete-chat chat-id)
             (not debug?)
             (assoc :data-store/deactivate-chat chat-id))]
    (handlers/merge-fx cofx fx (transport/unsubscribe-from-chat chat-id))))

(defn bot-only-chat? [db chat-id]
  (let [{:keys [group-chat contacts]} (get-in db [:chats chat-id])]
    (and (not group-chat)
         (get-in db [:contacts/contacts (:identity (first contacts)) :dapp?]))))
