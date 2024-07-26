(ns utils.transforms-test
  (:require
    [cljs.test :refer [are deftest is testing]]
    [utils.transforms :as sut]))

(defn map=
  [m1 m2]
  (= (js/JSON.stringify (clj->js m1))
     (js/JSON.stringify (clj->js m2))))

(deftest <-js-map-test
  (testing "without transforming keys/values"
    (are [expected m]
     (is (map= expected (sut/<-js-map m)))
     nil               nil
     nil               #js []
     #js {}            #js {}
     #js {"a" 1 "b" 2} #js {"a" 1 "b" 2}))

  (testing "with key/value transformation"
    (is (map= {"aa" [1] "bb" [2]}
              (sut/<-js-map #js {"a" 1 "b" 2}
                            {:key-fn (fn [k] (str k k))
                             :val-fn (fn [_ v] (vector v))}))))

  (testing "it is non-recursive"
    (is (map= {:a 1 :b #js {"c" 3}}
              (sut/<-js-map #js {"a" 1 "b" #js {"c" 3}}
                            {:key-fn (fn [k] (keyword k))}))))

  (testing "value transformation based on the key"
    (is (map= {"a" 1 "b" "banana"}
              (sut/<-js-map #js {"a" 1 "b" 2}
                            {:val-fn (fn [k v]
                                       (if (= "b" k)
                                         "banana"
                                         v))})))))
