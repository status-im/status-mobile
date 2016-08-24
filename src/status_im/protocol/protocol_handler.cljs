(ns status-im.protocol.protocol-handler
  (:require [status-im.utils.logging :as log]
            [status-im.constants :refer [ethereum-rpc-url]]
            [re-frame.core :refer [dispatch]]
            [status-im.models.protocol :refer [stored-identity]]
            [status-im.persistence.simple-kv-store :as kv]
            [status-im.models.chats :refer [active-group-chats]]))


(defn make-handler [db]
  {:ethereum-rpc-url ethereum-rpc-url
   :identity         (stored-identity db)
   ;; :active-group-ids is never used in protocol
   :active-group-ids (active-group-chats)
   :storage          kv/kv-store
   :handler          (fn [{:keys [event-type] :as event}]
                       (case event-type
                         :initialized (let [{:keys [identity]} event]
                                        (dispatch [:protocol-initialized identity]))
                         :message-received (let [{:keys [from to payload]} event]
                                             (dispatch [:received-message (assoc payload :chat-id from
                                                                                         :from from
                                                                                         :to to)]))
                         :message-delivered (let [{:keys [message-id from]} event]
                                              (dispatch [:message-delivered from message-id]))
                         :message-seen (let [{:keys [message-id from]} event]
                                         (dispatch [:message-seen from message-id]))
                         :message-failed (let [{:keys [message-id chat-id] :as event} event]
                                           (dispatch [:message-failed chat-id message-id]))
                         :message-sent (let [{:keys [message-id chat-id]} event]
                                         (dispatch [:message-sent chat-id message-id]))
                         :pending-message-upsert (let [{message :message} event]
                                                   (dispatch [:pending-message-upsert message]))
                         :pending-message-remove (let [{:keys [message-id]} event]
                                                   (dispatch [:pending-message-remove message-id]))
                         :new-group-chat (let [{:keys [from group-id identities group-name]} event]
                                           (dispatch [:group-chat-invite-received from group-id identities group-name]))
                         :new-group-message (let [{from     :from
                                                   group-id :group-id
                                                   payload  :payload} event]
                                              (dispatch [:received-message (assoc payload
                                                                             :chat-id group-id
                                                                             :from from)]))
                         :group-chat-invite-acked (let [{:keys [from group-id ack-message-id]} event]
                                                    (dispatch [:group-chat-invite-acked from group-id ack-message-id]))
                         :group-new-participant (let [{:keys [group-id identity from message-id]} event]
                                                  (dispatch [:participant-invited-to-group from group-id identity message-id]))
                         :group-removed-participant (let [{:keys [group-id identity from message-id]} event]
                                                      (dispatch [:participant-removed-from-group from group-id identity message-id]))
                         :removed-from-group (let [{:keys [group-id from message-id]} event]
                                               (dispatch [:you-removed-from-group from group-id message-id]))
                         :participant-left-group (let [{:keys [group-id from message-id]} event]
                                                   (dispatch [:participant-left-group from group-id message-id]))
                         :discover-response (let [{:keys [from payload]} event]
                                              (dispatch [:discovery-response-received from payload]))
                         :contact-update (let [{:keys [from payload]} event]
                                           (dispatch [:contact-update-received from payload]))
                         :contact-online (let [{:keys [from payload]} event]
                                           (dispatch [:contact-online-received from payload]))
                         (log/info "Don't know how to handle" event-type)))})
