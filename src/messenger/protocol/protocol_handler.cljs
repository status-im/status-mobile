(ns messenger.protocol.protocol-handler
  (:require [syng-im.utils.logging :as log]
            [messenger.constants :refer [ethereum-rpc-url]]
            [messenger.comm.intercom :refer [protocol-initialized]]
            [messenger.models.protocol :refer [current-identity]]))

(defn make-handler []
  {:ethereum-rpc-url ethereum-rpc-url
   :identity         (current-identity)
   :storage          nil
   :handler          (fn [{:keys [event-type] :as event}]
                       (log/info "Event:" (clj->js event))
                       (case event-type
                         :initialized (let [{:keys [identity]} event]
                                        (protocol-initialized identity))
                         ;:new-msg (let [{from               :from
                         ;                {content :content} :payload} event]
                         ;           (add-to-chat "chat" from content))
                         ;:msg-acked (let [{:keys [msg-id]} event]
                         ;             (add-to-chat "chat" ":" (str "Message " msg-id " was acked")))
                         ;:delivery-failed (let [{:keys [msg-id]} event]
                         ;                   (add-to-chat "chat" ":" (str "Delivery of message " msg-id " failed")))
                         ;:new-group-chat (let [{:keys [from group-id identities]} event]
                         ;                  (set-group-id! group-id)
                         ;                  (set-group-identities identities)
                         ;                  (add-to-chat "group-chat" ":" (str "Received group chat invitation from " from " for group-id: " group-id)))
                         ;:group-chat-invite-acked (let [{:keys [from group-id]} event]
                         ;                           (add-to-chat "group-chat" ":" (str "Received ACK for group chat invitation from " from " for group-id: " group-id)))
                         ;:new-group-msg (let [{from               :from
                         ;                      {content :content} :payload} event]
                         ;                 (add-to-chat "group-chat" from content))
                         ;:group-new-participant (let [{:keys [group-id identity from]} event]
                         ;                         (add-to-chat "group-chat" ":" (str (shorten from) " added " (shorten identity) " to group chat"))
                         ;                         (add-identity-to-group-list identity))
                         ;:group-removed-participant (let [{:keys [group-id identity from]} event]
                         ;                             (add-to-chat "group-chat" ":" (str (shorten from) " removed " (shorten identity) " from group chat"))
                         ;                             (remove-identity-from-group-list identity))
                         ;:removed-from-group (let [{:keys [group-id from]} event]
                         ;                      (add-to-chat "group-chat" ":" (str (shorten from) " removed you from group chat")))
                         ;:participant-left-group (let [{:keys [group-id from]} event]
                         ;                          (add-to-chat "group-chat" ":" (str (shorten from) " left group chat")))
                         ;(add-to-chat "chat" ":" (str "Don't know how to handle " event-type))
                         (log/info "Don't know how to handle" event-type)
                         ))})
