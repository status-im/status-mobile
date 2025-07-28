(ns utils.collection-test
  (:require
    [cljs.test :refer-macros [deftest is]]
    [utils.collection :as c]))

(deftest first-index-test
  (is (= 2
         (c/first-index (partial = :test)
                        '(:a :b :test :c :test))))
  (is (= nil
         (c/first-index (partial = :test)
                        '(:a :b :c)))))
