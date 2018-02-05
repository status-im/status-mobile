(ns status-im.protocol.web3.delivery
  (:require [cljs.core.async :as async]
            [status-im.protocol.web3.utils :as u]
            [cljs.spec.alpha :as s]
            [taoensso.timbre :refer-macros [debug] :as log]
            [status-im.protocol.validation :refer-macros [valid?]]
            [clojure.set :as set]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.random :as random]))



(s/def ::pos-int (s/and pos? int?))
(s/def ::delivery-loop-ms-interval ::pos-int)
(s/def ::ack-not-received-s-interval ::pos-int)
(s/def ::max-attempts-number ::pos-int)
(s/def ::default-ttl ::pos-int)
(s/def ::send-online-s-interval ::pos-int)
(s/def ::online-message fn?)
(s/def ::post-error-callback fn?)

(s/def ::delivery-options
  (s/keys :req-un [::delivery-loop-ms-interval ::ack-not-received-s-interval
                   ::max-attempts-number ::default-ttl ::send-online-s-interval
                   ::post-error-callback]
          :opt-un [::online-message]))

(s/def :delivery/pending-message
  (s/keys :req-un [:message/sig :message/to :shh/payload :payload/ack? ::id
                   :message/requires-ack? :message/topic ::attempts ::was-sent?]
          :opt-un [:message/pub-key :message/sym-key-password]))

(s/def :shh/pending-message
  (s/keys :req-un [:message/sig :shh/payload :message/topic]
          :opt-un [:message/ttl :message/pubKey :message/symKeyID]))

;; (when (valid? :shh/pending-message message')
;;   (let [group-id        (get-in message [:payload :group-id])
;;         pending-message {:id            message-id
;;                          :ack?          (boolean ack?)
;;                          :message       message'
;;                          :to            to
;;                          :type          type
;;                          :group-id      group-id
;;                          :requires-ack? (boolean requires-ack?)
;;                          :attempts      0
;;                          :was-sent?     false}]))

;; (select-keys message [:message-id :requires-ack? :type :clock-value])

;; (assoc message :was-sent? true
;;        :attempts 1)
;; (assoc data :attempts (inc attempts)
;;        :last-attempt (u/timestamp))

(defn should-be-retransmitted?
  "Checks if messages should be transmitted again."
  [{:keys [ack-not-received-s-interval max-attempts-number]}
   {:keys [was-sent? attempts last-attempt]}]
  (if-not was-sent?
    ;; message was not sent succesfully via web3.shh, but maybe
    ;; better to do this only when we receive error from shh.post
    ;; todo add some notification about network issues
    (<= attempts (* 5 max-attempts-number))
    (and
     ;; if message was not send less then max-attempts-number times
     ;; continue attempts
     (<= attempts max-attempts-number)
     ;; check retransmission interval
     (<= (+ last-attempt (* 1000 ack-not-received-s-interval)) (u/timestamp)))))

(defn- check-ttl
  [message message-type ttl-config default-ttl]
  (update message :ttl #(or % ((keyword message-type) ttl-config) default-ttl)))


#_(handlers/register-handler-fx
    :protocol/send-online
    [{:keys [db]} [this-event]]
    (let [{:keys [web3]} db
          send-online-s-interval 10]
      {:whisper/send-online-message {}
       ::dispatch-later {:ms (* 1000 send-online-s-interval)
                         :dispatch [this-event]}}))
