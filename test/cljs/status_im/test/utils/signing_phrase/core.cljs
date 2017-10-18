(ns status-im.test.utils.signing-phrase.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.signing-phrase.core :refer [generate]]
            [clojure.string :as string]))

(deftest test-generate
  (doseq [_ (range 30)]
    (let [result (generate)]
      (is (not (string/starts-with? result  " ")))
      (is (not (string/ends-with? result  " ")))
      (is (= (get (frequencies result) " ")) 2))))
