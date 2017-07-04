(ns status-im.protocol.chat
  (:require [cljs.spec.alpha :as s]
            [status-im.protocol.web3.filtering :as f]
            [status-im.protocol.web3.delivery :as d]
            [taoensso.timbre :refer-macros [debug]]
            [status-im.protocol.validation :refer-macros [valid?]]))

(def message-defaults
  {:topics [f/status-topic]})

(s/def ::timestamp int?)
(s/def ::user-message
  (s/merge
    :protocol/message
    (s/keys :req-un [:message/to :chat-message/payload])))

(defn send!
  [{:keys [web3 message]}]
  {:pre [(valid? ::user-message message)]}
  (let [message' (merge message-defaults
                        (assoc message
                          :type :message
                          :requires-ack? true))]
    (debug :send-user-message message')
    (d/add-pending-message! web3 message')))

(s/def ::seen-message
  (s/merge :protocol/message (s/keys :req-un [:message/to])))

(defn send-seen!
  [{:keys [web3 message]}]
  {:pre [(valid? ::seen-message message)]}
  (debug :send-seen message)
  (d/add-pending-message!
    web3
    (merge message-defaults
           (-> message
               (assoc
                 :type :seen
                 :requires-ack? false)
               (assoc-in [:payload :group-id] (:group-id message))
               (dissoc :group-id)))))

(defn send-clock-value-request!
  [{:keys [web3 message]}]
  (debug :send-clock-value-request message)
  (d/add-pending-message!
    web3
    (merge message-defaults
           (assoc message
             :type :clock-value-request
             :requires-ack? false))))

(defn send-clock-value!
  [{:keys [web3 message]}]
  (debug :send-clock-value message)
  (d/add-pending-message!
    web3
    (merge message-defaults
           (assoc message
             :type :clock-value
             :requires-ack? false))))
