(ns status-im.test.utils.pre-receiver
  (:require-macros [cljs.core.async.macros :as async])
  (:require [cljs.test :refer-macros [deftest is testing async]]
            [cljs.core.async :as async]
            [status-im.utils.pre-receiver :as pre-receiver]))

;; The tests in clocks.cljs only ensure that the local clock value is respected
;; and that new messages are always appended correctly so we get a locally
;; consistent view.

;; Additionally, a desirable property to have is that two people talking to each
;; other have roughly the same ordering of messages. Example:

;; A and B are different chats with different chat identifiers. The sent
;; clock-value represents that client's truth, but The Network (Whisper, etc)
;; doesn't guarantee _delivery_ order. This means a client can receive the
;; messages in the following ordering.
(def messages [{:id "a" :clock-value 1 :payload "a1"}
               {:id "a" :clock-value 2 :payload "a2"}
               {:id "b" :clock-value 1 :payload "b1"}
               {:id "a" :clock-value 4 :payload "a4"}
               {:id "a" :clock-value 3 :payload "a3"}
               {:id "b" :clock-value 2 :payload "b2"}])

;; Empirically speaking, "a4" arriving before "a3" happens when messages are
;; sent in quick succession, but the delay between these being delivered is
;; usually very small, i.e. <100ms.

;; Given this delivery order, we have a design decision to make. We can either
;; eagerly "commit" them, and thus update our local clock value to reflect the
;; order we see messages in. Alternatively, we can pause the commit/full receive
;; step and wait for some time for logically earlier messages arrive.

;; In 0.9.12 and earlier this is the behavior we had the former behavior, but
;; this breaks users expectation. The tests below showcases the latter behavior,
;; which can be turned on with a flag.

;; Invariant to maintain
(defn monotonic-increase? [received id clock-value]
  (->> received
       (filter (fn [[_ x]] (= x id)))
       sort
       last
       first
       ((fn [v] (or (nil? v) (> clock-value v))))))

(defn add-message-test [received invariant? {:keys [id clock-value] :as msg}]
  (when (not (monotonic-increase? @received id clock-value))
    (println "add-message-test NOT earliest clock value seen!")
    (println "add-message-test received:" (pr-str @received))
    (println "add-message-test new:" id clock-value)
    (is (not invariant?)))
  (swap! received conj [clock-value id]))

(defn simulate! [{:keys [reorder? invariant? done]}]
  (let [delay-ms 50
        received (atom #{})
        add-fn   (partial add-message-test received invariant?)
        in-ch    (pre-receiver/start! {:delay-ms delay-ms
                                       :reorder? reorder?
                                       :add-fn   add-fn})]

    (doseq [msg messages]
      (async/put! in-ch msg))

    (async/go (async/<! (async/timeout (* delay-ms 2)))
              (let [total (count messages)
                    poss  (count @received)]
                ;;(println "received" poss "/" total "messages")
                (reset! received #{})
                (done)))))

(deftest pre-receiver
  (testing "Pre-receiver with reorder - good case"
    (async done
           (simulate! {:reorder? true :invariant? true :done done})))

  ;; By setting invariant? to true this test will fail
  (testing "Pre-receiver without reorder - bad case"
    (async done
           (simulate! {:reorder? false :invariant? false :done done}))))
