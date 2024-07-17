(ns utils.vector-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [utils.vector :as vector]))

(deftest insert-element-at-test
  (testing "Inserting into an empty vector"
    (is (= [42] (vector/insert-element-at [] 42 0))))

  (testing "Inserting at the beginning of a vector"
    (is (= [42 1 2 3] (vector/insert-element-at [1 2 3] 42 0))))

  (testing "Inserting in the middle of a vector"
    (is (= [1 42 2 3] (vector/insert-element-at [1 2 3] 42 1))))

  (testing "Inserting at the end of a vector"
    (is (= [1 2 3 42] (vector/insert-element-at [1 2 3] 42 3)))))
