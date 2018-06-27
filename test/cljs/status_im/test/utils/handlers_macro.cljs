(ns status-im.test.utils.handlers-macro
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.handlers-macro :as m]))

(deftest merge-fx
  (letfn [(add-b [cofx]
            (assoc-in cofx [:db :b] "b"))
          (add-c [cofx]
                 (assoc-in cofx [:db :c] "c"))
          (add-tx [tx cofx]
                  (assoc cofx :data-store/tx [tx]))]
    (testing "it updates db correctly"
      (let [actual (m/merge-fx {:db {:a "a"}}
                               (add-b)
                               (add-c))]
        (is (= {:db {:a "a"
                     :b "b"
                     :c "c"}} actual))))
    (testing "it updates db correctly when a fn don't update it"
      (let [empty-fn (constantly nil)
            actual (m/merge-fx {:db {:a "a"}}
                               (add-b)
                               (empty-fn)
                               (add-c))]
        (is (= {:db {:a "a"
                     :b "b"
                     :c "c"}} actual))))
    #_(testing "it updates data-store/tx correctly"
        (let [actual (m/merge-fx {:data-store/tx [1]}
                                 (add-tx 2)
                                 (add-tx 3))]
          (is (= {:data-store/tx [1 2 3]} actual))))))
