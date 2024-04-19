(ns legacy.status-im.utils.signing-phrase.core-test
  (:require
    [cljs.test :refer-macros [deftest is]]
    [clojure.string :as string]
    [legacy.status-im.utils.signing-phrase.core :refer [generate]]))

(deftest test-generate
  (doseq [_ (range 30)]
    (let [result (generate)]
      (is (not (string/starts-with? result " ")))
      (is (not (string/ends-with? result " ")))
      (is (= (get (frequencies result) " ") 2)))))
