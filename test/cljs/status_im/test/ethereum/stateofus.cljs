(ns status-im.test.ethereum.stateofus
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.ethereum.stateofus :as stateofus]))

(deftest valid-username?
  (is (false? (stateofus/valid-username? nil)))
  (is (true? (stateofus/valid-username? "andrey")))
  (is (false? (stateofus/valid-username? "Andrey")))
  (is (true? (stateofus/valid-username? "andrey12"))))
