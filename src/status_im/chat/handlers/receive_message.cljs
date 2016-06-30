(ns status-im.chat.handlers.receive-message
  (:require [status-im.utils.handlers :refer [register-handler] :as u]
            [re-frame.core :refer [enrich after debug dispatch path]]
            [status-im.models.messages :as messages]
            [status-im.chat.utils :as cu]
            [status-im.commands.utils :refer [generate-hiccup]]
            [status-im.constants :refer [content-type-command-request]]
            [cljs.reader :refer [read-string]]
            [status-im.models.chats :as c]))

(defn check-previev [{:keys [content] :as message}]
  (if-let [preview (:preview content)]
    (let [rendered-preview (generate-hiccup (read-string preview))]
      (assoc message
        :preview preview
        :rendered-preview rendered-preview))
    message))

(defn store-message [{chat-id :from :as message}]
  (messages/save-message chat-id (dissoc message :rendered-preview :new?)))

(register-handler :received-message
  (u/side-effect!
    (fn [{:keys [chats] :as db} [_ {chat-id :from :keys [msg-id] :as message}]]
      (let [same-message (messages/get-message msg-id)]
        (when-not same-message
          (let [message' (assoc (->> message
                                     (cu/check-author-direction db chat-id)
                                     (check-previev))
                           :delivery-status :pending)]
            (store-message message')
            (when-not (c/chat-exists? chat-id)
              (dispatch [:add-chat chat-id]))
            (dispatch [::add-message message'])
            (when (= (:content-type message') content-type-command-request)
              (dispatch [:add-request chat-id message']))
            (dispatch [:add-unviewed-message chat-id msg-id])))))))

(register-handler ::add-message
  (fn [db [_ {chat-id :from :keys [new?] :as message}]]
    (cu/add-message-to-db db chat-id message new?)))
