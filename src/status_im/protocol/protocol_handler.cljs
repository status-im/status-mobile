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
                       (log/info "Event:" (clj->js event))
                       (case event-type
                         :initialized (let [{:keys [identity]} event]
                                        (dispatch [:protocol-initialized identity]))
                         :new-msg (let [{:keys [from to payload]} event]
                                    (dispatch [:received-msg (assoc payload :from from :to to)]))
                         :msg-acked (let [{:keys [msg-id from]} event]
                                      (dispatch [:acked-msg from msg-id]))
                         :msg-seen (let [{:keys [msg-id from]} event]
                                      (dispatch [:msg-seen from msg-id]))
                         :delivery-failed (let [{:keys [msg-id from]} event]
                                            (dispatch [:msg-delivery-failed from msg-id]))
                         :new-group-chat (let [{:keys [from group-id identities group-name]} event]
                                           (dispatch [:group-chat-invite-received from group-id identities group-name]))
                         :new-group-msg (let [{from     :from
                                               group-id :group-id
                                               payload  :payload} event]
                                          (dispatch [:group-received-msg (assoc payload :from from
                                                                                        :group-id group-id)]))
                         :group-chat-invite-acked (let [{:keys [from group-id ack-msg-id]} event]
                                                    (dispatch [:group-chat-invite-acked from group-id ack-msg-id]))
                         :group-new-participant (let [{:keys [group-id identity from msg-id]} event]
                                                  (dispatch [:participant-invited-to-group from group-id identity msg-id]))
                         :group-removed-participant (let [{:keys [group-id identity from msg-id]} event]
                                                      (dispatch [:participant-removed-from-group from group-id identity msg-id]))
                         :removed-from-group (let [{:keys [group-id from msg-id]} event]
                                               (dispatch [:you-removed-from-group from group-id msg-id]))
                         :participant-left-group (let [{:keys [group-id from msg-id]} event]
                                                   (dispatch [:participant-left-group from group-id msg-id]))
                         :discover-response (let [{:keys [from payload]} event]
                                              (dispatch [:discovery-response-received from payload]))
                         :contact-update (let [{:keys [from payload]} event]
                                           (dispatch [:contact-update-received from payload]))
                         :contact-online (let [{:keys [from payload]} event]
                                           (dispatch [:contact-online-received from payload]))
                         (log/info "Don't know how to handle" event-type)))})
