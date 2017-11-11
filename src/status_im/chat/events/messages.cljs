(ns status-im.chat.events.messages
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.chat.models :as model]
            [status-im.chat.models.unviewed-messages :as unviewed-messages-model]
            [status-im.chat.sign-up :as sign-up]
            [status-im.chat.constants :as chat-const]
            [status-im.chat.utils :as chat-utils]
            [status-im.data-store.handler-data :as handler-data]
            [status-im.data-store.messages :as msg-store]
            [status-im.data-store.contacts :as contacts-store]
            [status-im.data-store.chats :as chats-store]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.protocol.core :as protocol]
            [status-im.constants :as const]
            [status-im.ui.components.list-selection :as list-selection]))

;;;; Helper fns

(defn add-message
  [{:keys [db save-entities]} chat-id message]
  (let [message' (dissoc message :new?)]
    {:db            (-> db
                      (chat-utils/add-message-to-db chat-id chat-id message (:new? enriched-message))
                      (unviewed-messages-model/add-unviewed-message chat-id (:message-id message))
                      (assoc-in [:chats chat-id :last-message] message))
     :save-entities (conj (or save-entities []) [:message message'])}))

;;;; Handlers

(handlers/register-handler-db
  :chat-messages/set-shown
  [re-frame/trim-v]
  (fn [db [{:keys [chat-id message-id]}]]
    (update-in db
      [:chats chat-id :messages]
      (fn [messages]
        (map (fn [message]
               (if (= message-id (:message-id message))
                 (assoc message :new? false)
                 message))
          messages)))))

(handlers/register-handler-fx
  :chat-messages/send-seen!
  [re-frame/trim-v]
  (fn [{:keys [db]} [{:keys [message-id chat-id from]}]]
    (let [{:keys [web3 current-public-key chats]
           :contacts/keys [contacts]} db
          {:keys [group-chat public?]} (get chats chat-id)]
      (cond-> {:db (unviewed-messages-model/remove-unviewed-messages db chat-id)
               :update-message {:message-id     message-id
                                :message-status :seen}}
        (and (not (get-in contacts [chat-id] :dapp?))
          (not public?))
        (assoc :protocol-send-seen
               {:web3    web3
                :message (cond-> {:from       current-public-key
                                  :to         from
                                  :message-id message-id}
                           group-chat (assoc :group-id chat-id))})))))

;; TODO(alwx): can be restructured
(handlers/register-handler-fx
  :chat-messages/show-mnemonic
  [(re-frame/inject-cofx :get-db-message) re-frame/trim-v]
  (fn [{:keys [get-db-message]} [mnemonic signing-phrase]]
    (let [crazy-math-message? (get-db-message chat-const/crazy-math-message-id)]
      {:dispatch-n (sign-up/passphrase-messages-events mnemonic
                     signing-phrase
                     crazy-math-message?)})))