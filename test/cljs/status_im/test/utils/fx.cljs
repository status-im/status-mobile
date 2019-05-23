(ns status-im.test.utils.fx
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.fx :as fx]))

(fx/defn hello
  "this is a very nice useless function"
  [{:keys [db]} a]
  {:db (assoc db :a a)})

(fx/defn hello2
  {:doc "this function is useless as well"}
  [{:keys [db]} b]
  {:db (assoc db :a b) :b (:a db)})

(fx/defn hello3
  {:doc "lol lazy function does nothing"}
  [{:keys [db]} b]
  (identity nil))

(fx/defn hello4
  {:doc "this function is useless as well"
   :events [:test/valid1 :test/valid2]}
  [{:keys [db]} b]
  {:db (assoc db :a b) :b (:a db)})

(fx/defn hello5
  "lol lazy function does nothing"
  {:events [:test]}
  [{:keys [db]} b]
  (identity nil))

(deftest merge-fxs-test
  (testing "merge function for fxs"
    (let [cofx {:db {:c 2}}]
      (is (= (fx/merge cofx
                       (hello "a")
                       (hello2 "b"))
             {:db {:c 2
                   :a "b"}
              :b "a"}))
      (testing "with initial fxs map"
        (is (= (fx/merge cofx
                         {:potatoe :potating}
                         (hello "a")
                         (hello2 "b"))
               {:db {:c 2
                     :a "b"}
                :b "a"
                :potatoe :potating})
            "initial fxs map should be merged in the result"))
      (testing "with a nil producing function"
        (is (= (fx/merge cofx
                         (hello "a")
                         (hello3 "c")
                         (hello2 "b"))
               {:db {:c 2
                     :a "b"}
                :b "a"})))
      (testing "with condition statement"
        (testing "false"
          (is (= (let [do-hello? false]
                   (fx/merge cofx
                             (when do-hello?
                               (hello "a"))
                             (hello2 "b")))
                 {:db {:c 2
                       :a "b"}
                  :b nil})
              "the conditional statement should not apply"))
        (testing "true"
          (is (= (let [do-hello? true]
                   (fx/merge cofx
                             (when do-hello?
                               (hello "a"))
                             (hello2 "b")))
                 {:db {:c 2
                       :a "b"}
                  :b "a"})
              "the conditional statement should apply"))))))
