(ns status-im.test.chat.models.message-list
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.constants :as const]
            [taoensso.tufte :as tufte :refer-macros (defnp p profiled profile)]
            [status-im.chat.models.loading :as l]

            [status-im.chat.models.message-list :as s]))

(deftest message-stream-tests
  (testing "building the list"
    (let [m1 {:from "1"
              :clock-value 1
              :message-id "message-1"
              :whisper-timestamp 1
              :outgoing false}
          m2 {:from "2"
              :clock-value 2
              :message-id "message-2"
              :whisper-timestamp 2
              :outgoing true}
          m3 {:from "2"
              :clock-value 3
              :message-id "message-3"
              :whisper-timestamp 3
              :outgoing true}
          messages (shuffle [m1 m2 m3])
          [actual-m1
           actual-m2
           actual-m3] (vals (s/build messages))]
      (testing "it sorts them correclty"
        (is (= "message-3" (:message-id actual-m1)))
        (is (= "message-2" (:message-id actual-m2)))
        (is (= "message-1" (:message-id actual-m3))))
      (testing "it marks only the first message as :first?"
        (is (:first? actual-m1))
        (is (not (:first? actual-m2)))
        (is (not (:first? actual-m3))))
      (testing "it marks the first outgoing message as :first-outgoing?"
        (is (:first-outgoing? actual-m1))
        (is (not (:first-outgoing? actual-m2)))
        (is (not (:first-outgoing? actual-m3))))
      (testing "it marks messages from the same author next to another with :first-in-group?"
        (is (:first-in-group? actual-m1))
        (is (not (:first-in-group? actual-m2)))
        (is (:first-in-group? actual-m3)))
      (testing "it marks messages with display-photo? when they are not outgoing and are first-in-group?"
        (is (not (:display-photo? actual-m1)))
        (is (not (:display-photo? actual-m2)))
        (is (:display-photo? actual-m3)))
      (testing "it marks the last message from the same author with :last-in-group?"
        (is (not (:last-in-group? actual-m1)))
        (is (:last-in-group? actual-m2))
        (is (:last-in-group? actual-m3))))))

(def ascending-range (mapv
                      #(let [i (+ 100000 %)]
                         {:clock-value i
                          :whisper-timestamp i
                          :timestamp i
                          :message-id (str i)})
                      (range 2000)))

(def descending-range (reverse ascending-range))

(def random-range (shuffle ascending-range))

(defnp build-message-list [messages]
  (s/build messages))

(defnp append-to-message-list [l message]
  (s/add l message))

(defnp prepend-to-message-list [l message]
  (s/add l message))

(defnp insert-close-to-head-message-list [l message]
  (s/add l message))

(defnp insert-middle-message-list [l message]
  (s/add l message))

(tufte/add-basic-println-handler! {:format-pstats-opts {:columns [:n-calls :mean :min :max :clock :total]
                                                        :format-id-fn name}})

(deftest ^:benchmark benchmark-list
  (let [messages (sort-by :timestamp (mapv (fn [i] (let [i (+ 100000 i 1)] {:timestamp i :clock-value i :message-id (str i) :whisper-timestamp i})) (range 100)))
        built-list (s/build messages)]
    (testing "prepending to list"
      (profile {} (dotimes [_ 10] (prepend-to-message-list
                                   built-list
                                   {:clock-value 200000
                                    :message-id "200000"
                                    :whisper-timestamp 21
                                    :timestamp 21}))))
    (testing "append to list"
      (profile {} (dotimes [_ 10] (append-to-message-list
                                   built-list
                                   {:clock-value 100000
                                    :message-id "100000"
                                    :whisper-timestamp 100000
                                    :timestamp 100000}))))
    (testing "insert close to head"
      (profile {} (dotimes [_ 10] (insert-close-to-head-message-list
                                   built-list
                                   {:clock-value 109970
                                    :message-id "109970"
                                    :whisper-timestamp 1000
                                    :timestamp 1000}))))
    (testing "insert into the middle list"
      (profile {} (dotimes [_ 10] (insert-middle-message-list
                                   built-list
                                   {:clock-value 105000
                                    :message-id "10500"
                                    :whisper-timestamp 1000
                                    :timestamp 1000}))))))

(deftest message-list
  (let [current-messages [{:clock-value 109
                           :message-id "109"
                           :timestamp 9
                           :whisper-timestamp 9}
                          {:clock-value 106
                           :message-id "106"
                           :timestamp 6
                           :whisper-timestamp 6}
                          {:clock-value 103
                           :message-id "103"
                           :timestamp 3
                           :whisper-timestamp 3}]
        current-list (s/build current-messages)]
    (testing "inserting a newer message"
      (let [new-message {:timestamp 12
                         :clock-value 112
                         :message-id "112"
                         :whisper-timestamp 12}]
        (is (= 112
               (-> (s/add current-list new-message)
                   vals
                   first
                   :clock-value)))))
    (testing "inserting an older message"
      (let [new-message {:timestamp 0
                         :clock-value 100
                         :message-id "100"
                         :whisper-timestamp 0}]
        (is (= 100
               (-> (s/add current-list new-message)
                   vals
                   last
                   :clock-value)))))
    (testing "inserting in the middle of the list"
      (let [new-message {:timestamp 7
                         :clock-value 107
                         :message-id "107"
                         :whisper-timestamp 7}]
        (is (= 107
               (-> (s/add current-list new-message)
                   vals
                   (nth 1)
                   :clock-value)))))
    (testing "inserting in the middle of the list, clock-value clash"
      (let [new-message {:timestamp 6
                         :clock-value 106
                         :message-id "106a"
                         :whisper-timestamp 6}]
        (is (= "106a"
               (-> (s/add current-list new-message)
                   vals
                   (nth 1)
                   :message-id)))))))
