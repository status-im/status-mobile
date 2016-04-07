(ns syng-im.protocol.protocol-handler
  (:require [syng-im.utils.logging :as log]
            [syng-im.constants :refer [ethereum-rpc-url]]
            [re-frame.core :refer [dispatch]]
            [syng-im.models.protocol :refer [stored-identity]]
            [syng-im.persistence.simple-kv-store :as kv]
            [syng-im.models.chats :refer [active-group-chats]]))


(defn make-handler [db]
  {:ethereum-rpc-url ethereum-rpc-url
   :identity         (stored-identity db)
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
                         :delivery-failed (let [{:keys [msg-id]} event]
                                            (dispatch [:msg-delivery-failed msg-id]))
                         :new-group-chat (let [{:keys [from group-id identities group-name]} event]
                                           (dispatch [:group-chat-invite-received from group-id identities group-name]))
                         :new-group-msg (let [{from     :from
                                               group-id :group-id
                                               payload  :payload} event]
                                          (dispatch [:group-received-msg (assoc payload :from from
                                                                                        :group-id group-id)]))
                         :group-chat-invite-acked (let [{:keys [from group-id ack-msg-id]} event]
                                                    (dispatch [:group-chat-invite-acked from group-id ack-msg-id]))
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
