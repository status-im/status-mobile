(ns status-im.protocol.web3.delivery
  (:require-macros [cljs.core.async.macros :refer [go-loop go]])
  (:require [cljs.core.async :refer [<! timeout]]
            [status-im.protocol.web3.transport :as t]
            [status-im.protocol.web3.utils :as u]
            [status-im.protocol.encryption :as e]
            [cljs.spec :as s]
            [taoensso.timbre :refer-macros [debug] :as log]
            [status-im.protocol.validation :refer-macros [valid?]]
            [clojure.set :as set]))

(defonce loop-state (atom nil))
(defonce messages (atom {}))

(defn prepare-message
  [{:keys [payload keypair to] :as message}]
  (debug :prepare-message!)
  (let [{:keys [public]} keypair

        content (:content payload)
        content' (if (and (not to) public content)
                   (e/encrypt public (prn-str content))
                   content)

        payload' (-> message
                     (select-keys [:message-id :requires-ack? :type])
                     (merge payload)
                     (assoc :content content')
                     prn-str
                     u/from-utf8)]
    (-> message (select-keys [:from :to :topics :ttl])
        (assoc :payload payload'))))

(s/def :shh/pending-message
  (s/keys :req-un [:message/from :shh/payload :message/topics]
          :opt-un [:message/ttl :message/to]))

(defonce pending-mesage-callback (atom nil))
(defonce recipient->pending-message (atom {}))

(defn set-pending-mesage-callback!
  [callback]
  (reset! pending-mesage-callback callback))

(defn add-pending-message!
  [web3 {:keys [type message-id requires-ack? to ack?] :as message}]
  {:pre [(valid? :protocol/message message)]}
  (go
    (debug :add-pending-message!)
    ;; encryption can take some time, better to run asynchronously
    (let [message' (prepare-message message)]
      (when (valid? :shh/pending-message message')
        (let [group-id (get-in message [:payload :group-id])
              pending-message {:id            message-id
                               :ack?          (boolean ack?)
                               :message       message'
                               :to            to
                               :type          type
                               :group-id      group-id
                               :requires-ack? (boolean requires-ack?)
                               :attempts      0
                               :was-sent?     false}]
          (when (and @pending-mesage-callback requires-ack?)
            (@pending-mesage-callback :pending pending-message))
          (swap! messages assoc-in [web3 message-id to] pending-message)
          (when to
            (swap! recipient->pending-message
                   update to set/union #{[web3 message-id to]})))))))

(s/def :delivery/pending-message
  (s/keys :req-un [:message/from :message/to :shh/payload
                   :message/requires-ack? :payload/ack? ::id :message/topics
                   ::attempts ::was-sent?]))

(defn add-prepeared-pending-message!
  [web3 {:keys [message-id to] :as pending-message}]
  {:pre [(valid? :delivery/pending-message pending-message)]}
  (debug :add-prepeared-pending-message!)
  (let [message (select-keys pending-message [:from :to :topics :payload])
        pending-message' (assoc pending-message :message message
                                                :id message-id)]
    (swap! messages assoc-in [web3 message-id to] pending-message')
    (when to
      (swap! recipient->pending-message
             update to set/union #{[web3 message-id to]}))))

(defn remove-pending-message! [web3 id to]
  (swap! messages update web3
         (fn [messages]
           (when messages
             (let [message (messages id)
                   ;; Message that is send without specified "from" option
                   ;; is stored in pending "messages" map as
                   ;; {message-id {nil message}}.
                   ;; When we receive the first ack for such message it is
                   ;; removed from pending messages adding of the nil key
                   ;; to the next dissoc form
                   ;; todo rewrite handling of ack message in more clear way
                   message' (dissoc message to nil)]
               (if (seq message')
                 (assoc messages id message')
                 (dissoc messages id))))))
  (when to
    (swap! recipient->pending-message
           update to set/difference #{[web3 id to]})))

(defn message-was-sent! [web3 id to]
  (let [messages' (swap! messages update web3
                         (fn [messages]
                           (let [message (get-in messages [id to])
                                 message' (when message
                                            (assoc message :was-sent? true
                                                           :attemps 1))]
                             (if message'
                               (assoc-in messages [id to] message')
                               messages))))]
    (when @pending-mesage-callback
      (let [message (get-in messages' [web3 id to])]
        (when message
          (@pending-mesage-callback :sent message))))))

(defn attempt-was-made! [web3 id to]
  (debug :attempt-was-made id)
  (swap! messages update-in [web3 id to]
         (fn [{:keys [attempts] :as data}]
           (assoc data :attempts (inc attempts)
                       :last-attempt (u/timestamp)))))

(defn delivery-callback
  [web3 {:keys [id requires-ack? to]}]
  (fn [error _]
    (when error (log/error :shh-post-error error))
    (when-not error
      (debug :delivery-callback)
      (message-was-sent! web3 id to)
      (when-not requires-ack?
        (remove-pending-message! web3 id to)))))

(s/def ::pos-int (s/and pos? int?))
(s/def ::delivery-loop-ms-interval ::pos-int)
(s/def ::ack-not-received-s-interval ::pos-int)
(s/def ::max-attempts-number ::pos-int)
(s/def ::default-ttl ::pos-int)
(s/def ::send-online-s-interval ::pos-int)
(s/def ::online-message fn?)

(s/def ::delivery-options
  (s/keys :req-un [::delivery-loop-ms-interval ::ack-not-received-s-interval
                   ::max-attempts-number ::default-ttl ::send-online-s-interval]
          :opt-un [::online-message]))

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
      ;; if message was not send lees then max-attempts-number times
      ;; continue attempts
      (<= attempts max-attempts-number)
      ;; check retransmition interval
      (<= (+ last-attempt (* 1000 ack-not-received-s-interval)) (u/timestamp)))))

(defn- check-ttl
  [message message-type ttl-config default-ttl]
  (update message :ttl #(or % ((keyword message-type) ttl-config) default-ttl)))

(defn run-delivery-loop!
  [web3 {:keys [delivery-loop-ms-interval default-ttl ttl-config
                send-online-s-interval online-message]
         :as   options}]
  {:pre [(valid? ::delivery-options options)]}
  (debug :run-delivery-loop!)
  (let [previous-stop-flag @loop-state
        stop? (atom false)]
    ;; stop previous delivery loop if it exists
    (when previous-stop-flag
      (reset! previous-stop-flag true))
    ;; reset stop flag for a new loop
    (reset! loop-state stop?)
    ;; go go!!!
    (debug :init-loop)
    (go-loop [_ nil]
      (doseq [[_ messages] (@messages web3)]
        (doseq [[_ {:keys [id message to type] :as data}] messages]
          ;; check each message asynchronously
          (go
            (when (should-be-retransmitted? options data)
              (try
                (let [message' (check-ttl message type ttl-config default-ttl)
                      callback (delivery-callback web3 data)]
                  (t/post-message! web3 message' callback))
                (catch :default err
                  (log/error :post-message-error err))
                (finally
                  (attempt-was-made! web3 id to)))))))
      (when-not @stop?
        (recur (<! (timeout delivery-loop-ms-interval)))))
    (go-loop [_ nil]
      (when-not @stop?
        (online-message)
        (recur (<! (timeout (* 1000 send-online-s-interval))))))))

(defn reset-pending-messages! [to]
  (doseq [key (@recipient->pending-message to)]
    (swap! messages
           (fn [messages]
             (when (get-in messages key)
               (update-in messages key assoc
                          :last-attempt 0
                          :attempts 0))))))

(defn reset-all-pending-messages! []
  (reset! messages {}))
