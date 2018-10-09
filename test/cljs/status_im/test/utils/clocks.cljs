(ns status-im.test.utils.clocks
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.clocks :as clocks]))

;; Messages are shown on a per-chat basis, ordered by the message clock-value.
;; See status-im-utils.clocks namespace for details.

;; We are not a monolith.
(def a (atom {:identity "a"}))
(def b (atom {:identity "b"}))
(def c (atom {:identity "c"}))

(declare recv!)

;; The network is unreliable.
(defn random-broadcast! [chat-id message]
  (when (> (rand-int 10) 5) (recv! a chat-id message))
  (when (> (rand-int 10) 5) (recv! b chat-id message))
  (when (> (rand-int 10) 5) (recv! c chat-id message)))

(defn get-last-clock-value
  [db chat-id]
  (if-let [messages (-> @db :chats chat-id :messages)]
    (-> (sort-by :clock-value > messages)
        first
        :clock-value)
    0))

(defn save! [db chat-id message]
  (swap! db
         (fn [state]
           (let [messages (-> state :chats chat-id :messages)]
             (assoc-in state [:chats chat-id :messages]
                       (conj messages message))))))

(defn send! [db chat-id message]
  (let [clock-value (get-last-clock-value db chat-id)
        prepared-message (assoc message :clock-value (clocks/send clock-value))]
    (save! db chat-id prepared-message)
    (random-broadcast! chat-id prepared-message)))

(defn recv! [db chat-id {:keys [clock-value] :as message}]
  (let [local-clock (get-last-clock-value db chat-id)
        new-clock   (clocks/receive clock-value local-clock)]
    (when-not (= (:from message) (:identity @db))
      (save! db chat-id (assoc message :clock-value new-clock)))))

(defn thread [db chat-id]
  (let [messages (-> @db :chats chat-id :messages)]
    (sort-by :clock-value < messages)))

(defn format-message [{:keys [from text]}]
  (str from ": " text ", "))

(defn format-thread [thread]
  (apply str (map format-message thread)))

;; Invariant we want to maintain.
(defn ordered-increasing-text? [thread]
  (let [xs (map :text thread)]
    (or (empty? xs) (apply < xs))))

(defn simulate! []
  (send! a :foo {:from "a" :text "1"})
  (send! a :foo {:from "a" :text "2"})

  (send! a :bar {:from "a" :text "1"})

  (send! b :foo {:from "b" :text "3"})
  (send! c :foo {:from "c" :text "4"})
  (send! a :foo {:from "a" :text "5"})

  (send! c :bar {:from "c" :text "7"}))

(deftest clocks
  (testing "Message order preserved"
    (simulate!)
    (is (ordered-increasing-text? (thread a :foo)))
    (is (ordered-increasing-text? (thread b :foo)))
    (is (ordered-increasing-text? (thread c :foo)))
    (is (ordered-increasing-text? (thread a :bar))))

  (testing "Bad thread recognized as such"
    (let [bad-thread '({:from "a", :text "1", :clock-value 1}
                       {:from "c", :text "4", :clock-value 1}
                       {:from "a", :text "2", :clock-value 2}
                       {:from "a", :text "5", :clock-value 8})]
      (is (not (ordered-increasing-text? bad-thread))))))

(deftest safe-timestamp
  (testing "it caps the timestamp when a value too large is provided"
    (is (< (clocks/receive js/Number.MAX_SAFE_INTEGER 0)
           js/Number.MAX_SAFE_INTEGER))))

(deftest safe-timestamp?-test
  (testing "it returns false for a high number"
    (is (not (clocks/safe-timestamp? js/Number.MAX_SAFE_INTEGER))))
  (testing "it returns true for a normal timestamp number"
    (is (clocks/safe-timestamp? (clocks/send 0)))))

  ;; Debugging
;;(println "******************************************")
;;(println "A's POV :foo" (format-thread (thread a :foo)))
;;(println "B's POV :foo" (format-thread (thread b :foo)))
;;(println "C's POV :foo" (format-thread (thread c :foo)))
;;(println "******************************************")
