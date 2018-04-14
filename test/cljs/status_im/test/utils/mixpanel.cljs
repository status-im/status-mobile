(ns status-im.test.utils.mixpanel
  (:require [cljs.test :refer [async deftest is]]
            [status-im.utils.mixpanel :as mixpanel]
            [cljs.core.async :as async]))

(deftest events
  (is (not (nil? mixpanel/events))))

(deftest matches?
  (is (true? (mixpanel/matches? [:key] [:key])))
  (is (false? (mixpanel/matches? [:key1] [:key2])))
  (is (true? (mixpanel/matches? [:key :subkey] [:key])))
  (is (false? (mixpanel/matches? [:key] [:key :subkey]))))

(def definitions {[:key] {:trigger [:key]} [:key :subkey] {:trigger [:key :subkey]}})

(deftest matching-event
  (is (empty? (mixpanel/matching-events [:non-existing] definitions)))
  (is (= 1 (count (mixpanel/matching-events [:key] definitions))))
  (is (= 2 (count (mixpanel/matching-events [:key :subkey] definitions))))
  (is (empty? (mixpanel/matching-events [:key1 :another-subkey] definitions))))

(deftest drain-events-queue!-test
  (async
   done
   (let [queue (async/chan (async/sliding-buffer 2000))
         results (atom [])]
     (async/go
       (async/<! (async/onto-chan queue (range 123) false))
       (async/<!
        (mixpanel/drain-events-queue!
         queue
         (fn [events]
           (let [result-chan (async/chan)]
             (swap! results conj events)
             (async/go (async/close! result-chan))
             result-chan))))
       (is (= @results [(range 50)
                        (range 50 100)
                        (range 100 123)]))
       (done)))))
