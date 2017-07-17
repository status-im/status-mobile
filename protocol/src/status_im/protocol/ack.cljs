(ns status-im.protocol.ack
  (:require [status-im.protocol.web3.delivery :as d]
            [status-im.protocol.web3.filtering :as f]
            [status-im.protocol.web3.utils :as u]))

(defn check-ack!
  [web3
   from
   {:keys [type requires-ack? message-id ack? group-id ack-of-message]}
   identity]
  (when (and requires-ack? (not ack?))
    (let [message {:from       identity
                   :to         from
                   :message-id (u/id)
                   :topics     [f/status-topic]
                   :type       type
                   :ack?       true
                   :payload    {:type           type
                                :ack?           true
                                :ack-of-message message-id
                                :group-id       group-id}}]
      (d/add-pending-message! web3 message)))
  (when ack?
    (d/remove-pending-message! web3 ack-of-message from)))
