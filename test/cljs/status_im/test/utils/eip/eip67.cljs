(ns status-im.test.utils.eip.eip67
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.eip.eip67 :as eip67]))

(deftest parse-uri
  (is (= nil (eip67/parse-uri nil)))
  (is (= nil (eip67/parse-uri "random")))
  (is (= nil (eip67/parse-uri "ethereum:")))
  (is (= nil (eip67/parse-uri "ethereum:?value=1")))
  (is (= nil (eip67/parse-uri "bitcoin:0x1234")))
  (is (= {:address "0x1234"} (eip67/parse-uri "ethereum:0x1234")))
  (is (= {:address "0x1234" :to "0x5678" :value "1"} (eip67/parse-uri "ethereum:0x1234?to=0x5678&value=1"))))

(deftest generate-uri
  (is (= nil (eip67/generate-uri nil)))
  (is (= "ethereum:0x1234" (eip67/generate-uri "0x1234")))
  (is (= "ethereum:0x1234?to=0x5678" (eip67/generate-uri "0x1234" {:to "0x5678"})))
  (is (= "ethereum:0x1234?to=0x5678&value=1" (eip67/generate-uri "0x1234" {:to "0x5678" :value 1}))))