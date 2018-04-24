(ns ^{:doc "Group chat API"}
    status-im.transport.message.v1.group-chat
  (:require [clojure.set :as set]
            [re-frame.core :as re-frame]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.transport.message.core :as message]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.group.core :as group]
            [status-im.chat.models :as models.chat]
            [status-im.chat.models.message :as models.message]
            [status-im.transport.message.v1.protocol :as protocol]
            [status-im.transport.utils :as transport.utils]))


;; NOTE: We ignore the chat-id from the send and receive method.
;; The chat-id is usually deduced from the filter the message comes from but not in that case because it is sent
;; individually to each participant of the group.
;; In order to be able to determine which group the message belongs to the chat-id is therefore
;; passed in the message itself
(defrecord NewGroupKey [chat-id sym-key message]
  message/StatusMessage
  (send [this _ cofx]
    (let [public-keys (get-in cofx [:db :chats chat-id :contacts])]
      (protocol/multi-send-with-pubkey {:public-keys public-keys
                                        :chat-id     chat-id
                                        :payload     this}
                                       cofx)))
  (receive [this _ signature {:keys [db] :as cofx}]
    (handlers-macro/merge-fx cofx
                       {:shh/add-new-sym-key {:web3       (:web3 db)
                                              :sym-key    sym-key
                                              :on-success (fn [sym-key sym-key-id]
                                                            (re-frame/dispatch [:group/add-new-sym-key {:chat-id    chat-id
                                                                                                   :signature  signature
                                                                                                   :sym-key    sym-key
                                                                                                   :sym-key-id sym-key-id
                                                                                                   :message    message}]))}}
                       (protocol/init-chat chat-id))))

(defn- user-is-group-admin? [chat-id cofx]
  (= (get-in cofx [:db :chats chat-id :group-admin])
     (get-in cofx [:db :current-public-key])))

(defn- send-new-group-key [message chat-id cofx]
  (when (user-is-group-admin? chat-id cofx)
    {:shh/get-new-sym-key {:web3 (get-in cofx [:db :web3])
                           :on-success (fn [sym-key sym-key-id]
                                         (re-frame/dispatch [:group/send-new-sym-key {:chat-id    chat-id
                                                                                 :sym-key    sym-key
                                                                                 :sym-key-id sym-key-id
                                                                                 :message    message}]))}}))

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

(defn- init-chat-if-new [chat-id cofx]
  (if (nil? (get-in cofx [:db :transport/chats chat-id]))
    (protocol/init-chat chat-id cofx)))

(defn- participants-diff [existing-participants-set new-participants-set]
  {:removed (set/difference existing-participants-set new-participants-set)
   :added   (set/difference new-participants-set existing-participants-set)})

(defrecord GroupAdminUpdate [chat-name participants]
  message/StatusMessage
  (send [this chat-id cofx]
    (handlers-macro/merge-fx cofx
                       (init-chat-if-new chat-id)
                       (send-new-group-key this chat-id)))
  (receive [this chat-id signature {:keys [now db random-id] :as cofx}]
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
              (handlers-macro/merge-fx cofx
                                 (models.message/receive
                                   (models.message/system-message chat-id random-id now
                                                                  (str admin-name " " (i18n/label :t/removed-from-chat))))
                                 (models.chat/upsert-chat {:chat-id         chat-id
                                                           :removed-from-at now
                                                           :is-active       false})
                                 (transport.utils/unsubscribe-from-chat chat-id))
              (handlers-macro/merge-fx cofx
                                 (models.message/receive
                                  (models.message/system-message chat-id random-id now
                                                                 (prepare-system-message  admin-name
                                                                                          added
                                                                                          removed
                                                                                          (:contacts/contacts db))))
                                 (group/participants-added chat-id added)
                                 (group/participants-removed chat-id removed)))))
        ;; first time we hear about chat -> create it if we are among participants
        (when (get (set participants) me)
          (models.chat/add-group-chat chat-id chat-name signature participants cofx))))))

(defrecord GroupLeave []
  message/StatusMessage
  (send [this chat-id cofx]
    (protocol/send {:chat-id       chat-id
                    :payload       this
                    :success-event [::unsubscribe-from-chat chat-id]}
                   cofx))
  (receive [this chat-id signature {:keys [db now random-id] :as cofx}]
    (let [me                       (:current-public-key db)
          participant-leaving-name (or (get-in db [:contacts/contacts signature :name])
                                       signature)]
      (when (get-in db [:chats chat-id]) ;; chat is present
        (handlers-macro/merge-fx cofx
                           (models.message/receive
                            (models.message/system-message chat-id random-id now
                                                           (str participant-leaving-name " " (i18n/label :t/left))))
                           (group/participants-removed chat-id #{signature})
                           (send-new-group-key nil chat-id))))))

