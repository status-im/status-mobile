(ns status-im.chat.handlers.receive-message
  (:require [status-im.utils.handlers :refer [register-handler] :as u]
            [re-frame.core :refer [enrich after debug dispatch path]]
            [status-im.data-store.messages :as messages]
            [status-im.chat.utils :as cu]
            [status-im.commands.utils :refer [generate-hiccup]]
            [status-im.constants :refer [content-type-command-request]]
            [cljs.reader :refer [read-string]]
            [status-im.data-store.chats :as chats]))

(defn check-preview [{:keys [content] :as message}]
  (if-let [preview (:preview content)]
    (let [rendered-preview (generate-hiccup (read-string preview))]
      (assoc message
        :preview preview
        :rendered-preview rendered-preview))
    message))

(defn store-message [{chat-id :chat-id :as message}]
  (messages/save chat-id (dissoc message :rendered-preview :new?)))

(defn get-current-identity
  [{:keys [current-account-id accounts]}]
  (:public-key (accounts current-account-id)))

(defn receive-message
  [db [_ {:keys [from group-id chat-id message-id] :as message}]]
  (let [same-message (messages/get-by-id message-id)
        current-identity (get-current-identity db)
        chat-id' (or group-id chat-id from)
        exists? (chats/exists? chat-id')
        active? (chats/is-active? chat-id')]
    (when (and (not same-message)
               (not= from current-identity)
               (or (not exists?) active?))
      (let [group-chat? (not (nil? group-id))
            previous-message (messages/get-last-message chat-id')
            message' (assoc (->> message
                                 (cu/check-author-direction previous-message)
                                 (check-preview))
                       :chat-id chat-id')]
        (store-message message')
        (when-not exists?
          (dispatch [:add-chat chat-id' (when group-chat? {:group-chat true})]))
        (dispatch [::add-message message'])
        (when (= (:content-type message') content-type-command-request)
          (dispatch [:add-request chat-id' message']))
        (dispatch [:add-unviewed-message chat-id' message-id])))))

(register-handler :received-protocol-message!
  (u/side-effect!
    (fn [_ [_ {:keys [from to payload]}]]
      (dispatch [:received-message (merge payload
                                          {:from    from
                                           :to      to
                                           :chat-id from})]))))

(register-handler :received-message
  (u/side-effect! receive-message))

(register-handler ::add-message
  (fn [db [_ {:keys [chat-id new?] :as message}]]
    (cu/add-message-to-db db chat-id message new?)))
