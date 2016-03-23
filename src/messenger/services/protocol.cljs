(ns messenger.services.protocol
  (:require [messenger.models.protocol :refer [set-initialized
                                               update-identity]]
            [messenger.models.messages :refer [save-message]]
            [messenger.models.user-data :refer [set-identity]]
            [syng-im.utils.logging :as log]
            [syng-im.protocol.api :as api]
            [messenger.omnext :as omnext]
            [om.next :as om]
            [messenger.constants :refer [text-content-type]]))

(defmulti protocol (fn [state id args]
                     id))

(defmethod protocol :protocol/initialized
  [state id {:keys [identity] :as args}]
  (log/debug "handling " id "args = " args)
  (update-identity identity)
  (set-identity identity)
  (set-initialized true))

(defmethod protocol :protocol/save-new-msg
  [state id {{from :from :as msg} :msg :as args}]
  (log/debug "handling " id "args = " args)
  (let [chat-id from]
    (om/transact! omnext/reconciler `[(chat/add-msg-to-chat {:msg     ~msg
                                                             :chat-id ~chat-id}) [:chat/messages :chat/chat-id :chat/chat]])))

(defmethod protocol :protocol/send-msg
  [state id {:keys [chat-id text] :as args}]
  (log/debug "handling " id "args = " args)
  (let [{msg-id     :msg-id
         {from :from
          to   :to} :msg} (api/send-user-msg {:to      chat-id
                                              :content text})
        msg {:msg-id       msg-id
             :from         from
             :to           to
             :content      text
             :content-type text-content-type
             :outgoing     true}]
    (om/transact! omnext/reconciler                         ;;(om/class->any omnext/reconciler messenger.components.chat.chat/Chat)
                  `[(chat/add-msg-to-chat {:msg     ~msg
                                           :chat-id ~chat-id}) [:chat/messages :chat/chat-id :chat/chat]])))

(defn protocol-handler [state [id args]]
  (log/debug "protocol-handler: " args)
  (protocol state id args))


(comment

  (om/transact! omnext/reconciler `[(chat/add-msg-to-chat {:msg     {:msg-id "1458670960090-ed5f995a-b686-5cbe-bf96-8a60ada8f6c3"}
                                                           :chat-id "1"}) [:chat/chat]])

  (om/get-indexer omnext/reconciler)
(om.next.protocols/reindex! omnext/reconciler)
  )