(ns ^{:doc "API for whisper filters"}
 status-im.transport.filters
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.transport.utils :as utils]
            [status-im.utils.config :as config]
            [status-im.utils.random :as random]
            [status-im.constants :as constants]
            [status-im.utils.random]
            [taoensso.timbre :as log]))

;; Number of different personal topics
(def n-partitions 5000)

(defn expected-number-of-collisions
  "Expected number of topic collision given the number of expected users,
  we want this value to be greater than a threshold to avoid positive
  identification given the attacker has a topic & public key.
  Used only for safety-checking n-partitions"
  [total-users]
  (+
   (- total-users
      n-partitions)
   (* n-partitions
      (js/Math.pow
       (/
        (- n-partitions 1)
        n-partitions)
       total-users))))

(defn partition-topic
  "Given a public key return a partitioned topic between 0 and n"
  [pk]
  (let [gen (random/rand-gen pk)]
    (-> (random/seeded-rand-int gen n-partitions)
        (str "-discovery")
        utils/get-topic)))

(def discovery-topic
  (utils/get-topic constants/contact-discovery))

(defn discovery-topics [pk]
  [(partition-topic pk)
   discovery-topic])

(defn- receive-message [chat-id js-error js-message]
  (re-frame/dispatch [:protocol/receive-whisper-message js-error js-message chat-id]))

(defn remove-filter! [filter]
  (.stopWatching filter
                 (fn [error _]
                   (if error
                     (log/warn :remove-filter-error filter error)
                     (log/debug :removed-filter filter))))
  (log/debug :stop-watching filter))

(defn add-shh-filter!
  [web3 options callback]
  (.newMessageFilter (utils/shh web3) (clj->js options)
                     callback
                     #(log/warn :add-filter-error (.stringify js/JSON (clj->js options)) %)))

(defn add-filter!
  [web3 {:keys [chat-id event]} raw-shh-options callback]
  (let [shh-options  (if config/offline-inbox-enabled?
                       (assoc raw-shh-options :allowP2P true)
                       raw-shh-options)]
    (log/debug :add-filter chat-id shh-options)
    (if-let [filter (add-shh-filter! web3 shh-options callback)]
      (re-frame/dispatch [event filter chat-id])
      (log/error "Could not create filter for" shh-options))))

(re-frame/reg-fx
 :shh/add-filter
 (fn [{:keys [web3 sym-key-id one-to-one topic chat-id]}]
   (add-filter! web3
                {:chat-id chat-id
                 :event ::filter-added}
                {:topics [topic]
                 :symKeyID sym-key-id}
                (partial receive-message chat-id))
   ;; We add a noop filter to avoid identification
   (when one-to-one
     (add-filter!
      web3
      {:chat-id chat-id
       :event ::filter-added}
      {:topics [(partition-topic chat-id)]
       :minPow 1
       :symKeyId sym-key-id}
      (constantly nil)))))

(handlers/register-handler-db
 ::filter-added
 [re-frame/trim-v]
 (fn [db [filter chat-id]]
   (update-in db [:transport/chats chat-id :filters] conj filter)))

(re-frame/reg-fx
 :shh/add-discovery-filter
 (fn [{:keys [web3 private-key-id topics]}]
   (add-filter! web3
                {:event ::discovery-filter-added}
                {:topics topics
                 :privateKeyID private-key-id}
                (partial receive-message nil))))

(handlers/register-handler-db
 ::discovery-filter-added
 [re-frame/trim-v]
 (fn [db [filter]]
   (assoc db :transport/discovery-filter filter)))

(re-frame/reg-fx
 :shh/remove-filter
 (fn [filter]
   (when filter (remove-filter! filter))))

(re-frame/reg-fx
 :shh/remove-filters
 (fn [filters]
   (doseq [filter filters]
     (remove-filter! filter))))
