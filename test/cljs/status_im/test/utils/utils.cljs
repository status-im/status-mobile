(ns status-im.test.utils.utils
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.utils.core :as u]
            [status-im.utils.utils :as utils]
            [status-im.models.browser :as browser]))

(deftest wrap-as-call-once-test
  (let [count (atom 0)]
    (letfn [(inc-count [] (swap! count inc))]
      (let [f (u/wrap-call-once! inc-count)]
        (is (nil? (f)))
        (is (= 1 @count))
        (is (nil? (f)))
        (is (= 1 @count))))))

(deftest truncate-str-test
  (is (= (u/truncate-str "Long string" 7) "Long..."))       ; threshold is less then string length
  (is (= (u/truncate-str "Long string" 7 true) "Lo...ng"))  ; threshold is less then string length (truncate middle)
  (is (= (u/truncate-str "Long string" 11) "Long string"))  ; threshold is the same as string length
  (is (= (u/truncate-str "Long string" 20) "Long string"))) ; threshold is more then string length

(deftest clean-text-test
  (is (= (u/clean-text "Hello! \n\r") "Hello!")
      (= (u/clean-text "Hello!") "Hello!")))

(deftest first-index-test
  (is (= 2 (u/first-index (partial = :test)
                          '(:a :b :test :c :test))))
  (is (= nil (u/first-index (partial = :test)
                            '(:a :b :c)))))

(deftest hash-tag?-test
  (is (u/hash-tag? "#clojure"))
  (is (not (u/hash-tag? "clojure")))
  (is (not (u/hash-tag? "clo#jure")))
  (is (not (u/hash-tag? "clojure#"))))

(deftest update-if-present-test
  (is (= {:a 1} (u/update-if-present {:a 0} :a inc)))
  (is (= {:a 2} (u/update-if-present {:a 0} :a + 2)))
  (is (= {:a 0} (u/update-if-present {:a 0} :b inc))))

(deftest map-values-test
  (is (= {} (u/map-values {} inc)))
  (is (= {:a 1} (u/map-values {:a 0} inc)))
  (is (= {:a 1 :b 2} (u/map-values {:a 0 :b 1} inc))))

(deftest deep-merge-test
  (is (= {} (u/deep-merge {} {})))
  (is (= {:a 1 :b 2} (u/deep-merge {:a 1} {:b 2})))
  (is (= {:a {:b 1 :c 2}}
         (u/deep-merge {:a {:b 1 :c 1}} {:a {:c 2}})))
  (is (= {:a {:b {:c 2}} :d 1}
         (u/deep-merge {:a {:b {:c 1}} :d 1} {:a {:b {:c 2}}}))))

;;; --- distinct-by-group-test ------------------------------------
;;;
;;; Utility developed originally to dedupe list of browsers visited
;;; for chat/home screens that list those.
;;; The following tests stress the de-duper and incidentally the
;;; browser/get-current-url mechanism by varying index of the
;;; current url, by having a dup fall in the middle, and by
;;; having a null test in which an entry is unduplicated.

(def ^:private browsers
  {"aaa-1"   {:br-id         "aaa-1"
              :history       ["aaa.com" "https://aaa.com"]
              :history-index 1
              :timing        [:latest]
              :timestamp     42}
   "aaa-2"   {:br-id         "aaa-2"
              :history       ["https://aaa.com"]
              :history-index 0
              :timing        [:earliest]
              :timestamp     0}

   "bbb-1"   {:br-id         "bbb-1"
              :history       ["bbb.com" "https://bbb.com"]
              :history-index 0
              :timing        [:earliest]
              :timestamp     42}

   "bbb-mid" {:br-id         "bbb-mid"
              :history       ["http://bbb.com" "bbb.com" "https://bbb.com"]
              :history-index 1
              :timing        [:middle]
              :timestamp     67}
   "bbb-2"   {:br-id         "bbb-2"
              :history       ["https://bbb.com" "bbb.com"]
              :history-index 1
              :timing        [:latest]
              :timestamp     99}

   ;; unduplicated entry as edge case
   "uniq-0"  {:br-id         "uniq-0"
              :history       ["https://uniq.com" "uniq.com"]
              :history-index 0
              :timing        [:earliest :latest]
              :timestamp     42}})

(deftest distinct-by-group-test
  ;; deduplicate map given key to identify dups and a key to identify
  ;; a property to be maximized when selecting amongst the dups"
  (let [result (utils/distinct-by-group browsers browser/get-current-url :timestamp)]
    (is (every? #(some #{:latest} %)
                (map (comp :timing second) result)))

    ;; sanity check just one for complete reproduction...
    (is (and (seq (get browsers "aaa-1"))
             (= (get browsers "aaa-1")
                (get result "aaa-1"))))

    ;; check edge case of unduplicated entry
    (is (and (seq (get browsers "uniq-0"))
             (= (get browsers "uniq-0")
                (get result "uniq-0")))))

  ;; same as above except minimizing
  (let [result (utils/distinct-by-group browsers browser/get-current-url :timestamp <)]
    (is (every? #(some #{:earliest} %)
                (map (comp :timing second) result)))
    (is (and (seq (get browsers "aaa-2"))
             (= (get browsers "aaa-2")
                (get result "aaa-2"))))
    (is (and (seq (get browsers "uniq-0"))
             (= (get browsers "uniq-0")
                (get result "uniq-0"))))))