(ns status-im.chat.handlers.receive-message
  (:require [status-im.utils.handlers :refer [register-handler] :as u]
            [re-frame.core :refer [enrich after debug dispatch path]]
            [status-im.models.messages :as messages]
            [status-im.chat.utils :as cu]
            [status-im.commands.utils :refer [generate-hiccup]]
            [status-im.constants :refer [content-type-command-request]]
            [cljs.reader :refer [read-string]]
            [status-im.models.chats :as c]))

(defn check-preview [{:keys [content] :as message}]
  (if-let [preview (:preview content)]
    (let [rendered-preview (generate-hiccup (read-string preview))]
      (assoc message
        :preview preview
        :rendered-preview rendered-preview))
    message))

(defn store-message [{chat-id :chat-id :as message}]
  (messages/save-message chat-id (dissoc message :rendered-preview :new?)))

(defn get-current-identity
  [{:keys [current-account-id accounts]}]
  (:public-key (accounts current-account-id)))

(defn receive-message
  [db [_ {:keys [from group-id chat-id message-id] :as message}]]
  (let [same-message (messages/get-message message-id)
        current-identity (get-current-identity db)]
    (when-not (or same-message (= from current-identity))
      (let [group-chat? (not (nil? group-id))
            chat-id' (or chat-id from)
            previous-message (messages/get-last-message chat-id')
            message' (assoc (->> message
                                 (cu/check-author-direction previous-message)
                                 (check-preview))
                       :delivery-status :sending
                       :chat-id chat-id')]
        (store-message message')
        (when-not (c/chat-exists? chat-id')
          (dispatch [:add-chat chat-id' (when group-chat? {:group-chat true})]))
        (dispatch [::add-message message'])
        (when (= (:content-type message') content-type-command-request)
          (dispatch [:add-request chat-id' message']))
        (dispatch [:add-unviewed-message chat-id' message-id])))))

(register-handler :received-message
  (u/side-effect! receive-message))

(register-handler ::add-message
  (fn [db [_ {:keys [chat-id new?] :as message}]]
    (cu/add-message-to-db db chat-id message new?)))
