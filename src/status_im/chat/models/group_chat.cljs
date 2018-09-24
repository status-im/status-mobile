(ns status-im.chat.models.group-chat
  (:require
   [clojure.set :as set]
   [status-im.i18n :as i18n]
   [status-im.transport.utils :as transport.utils]
   [status-im.ui.screens.group.core :as group]
   [status-im.chat.models :as models.chat]
   [status-im.transport.message.core :as message]
   [status-im.transport.message.v1.group-chat :as transport.group-chat]
   [status-im.chat.models.message :as models.message]
   [status-im.utils.fx :as fx]))

(defn- participants-diff [existing-participants-set new-participants-set]
  {:removed (set/difference existing-participants-set new-participants-set)
   :added   (set/difference new-participants-set existing-participants-set)})

(defn- prepare-system-message [admin-name added-participants removed-participants contacts]
  (let [added-participants-names   (map #(get-in contacts [% :name] %) added-participants)
        removed-participants-names (map #(get-in contacts [% :name] %) removed-participants)]
    (cond
      (and (seq added-participants) (seq removed-participants))
      (str admin-name " "
           (i18n/label :t/invited) " " (apply str (interpose ", " added-participants-names))
           " and "
           (i18n/label :t/removed) " " (apply str (interpose ", " removed-participants-names)))

      (seq added-participants)
      (str admin-name " " (i18n/label :t/invited) " " (apply str (interpose ", " added-participants-names)))

      (seq removed-participants)
      (str admin-name " " (i18n/label :t/removed) " " (apply str (interpose ", " removed-participants-names))))))

(defn handle-group-admin-update [{:keys [chat-name participants]} chat-id signature {:keys [now db random-id] :as cofx}]
  (let [me (:current-public-key db)]
    ;; we have to check if we already have a chat, or it's a new one
    (if-let [{:keys [group-admin contacts] :as chat} (get-in db [:chats chat-id])]
      ;; update for existing group chat
      (when (and (= signature group-admin)  ;; make sure that admin is the one making changes
                 (not= (set contacts) (set participants))) ;; make sure it's actually changing something
        (let [{:keys [removed added]} (participants-diff (set contacts) (set participants))
              admin-name              (or (get-in db [:contacts/contacts group-admin :name])
                                          group-admin)]
          (if (removed me) ;; we were removed
            (fx/merge cofx
                      (models.message/receive
                       (models.message/system-message chat-id random-id now
                                                      (str admin-name " " (i18n/label :t/removed-from-chat))))
                      (models.chat/upsert-chat {:chat-id         chat-id
                                                :removed-from-at now
                                                :is-active       false})
                      (transport.utils/unsubscribe-from-chat chat-id))
            (fx/merge cofx
                      (models.message/receive
                       (models.message/system-message chat-id random-id now
                                                      (prepare-system-message admin-name
                                                                              added
                                                                              removed
                                                                              (:contacts/contacts db))))
                      (group/participants-added chat-id added)
                      (group/participants-removed chat-id removed)))))
      ;; first time we hear about chat -> create it if we are among participants
      (when (get (set participants) me)
        (models.chat/add-group-chat cofx chat-id chat-name signature participants)))))

(fx/defn handle-group-leave
  [{:keys [db random-id now] :as cofx} chat-id signature]
  (let [me                       (:current-public-key db)
        participant-leaving-name (or (get-in db [:contacts/contacts signature :name])
                                     signature)]
    (when (and
           (not= signature me)
           (get-in db [:chats chat-id])) ;; chat is present
      (fx/merge cofx
                (models.message/receive
                 (models.message/system-message chat-id random-id now
                                                (str participant-leaving-name " " (i18n/label :t/left))))
                (group/participants-removed chat-id #{signature})
                (transport.group-chat/send-new-group-key nil chat-id)))))
