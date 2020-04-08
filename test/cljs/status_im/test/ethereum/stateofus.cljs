(ns status-im.test.ethereum.stateofus
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.ethereum.stateofus :as stateofus]))

(deftest valid-username?
  (is (false? (stateofus/valid-username? nil)))
  (is (true? (stateofus/valid-username? "andrey")))
  (is (false? (stateofus/valid-username? "Andrey")))
  (is (true? (stateofus/valid-username? "andrey12"))))

(deftest username-with-domain
  (is (nil? (stateofus/username-with-domain nil)))
  (is (= "andrey.stateofus.eth" (stateofus/username-with-domain "andrey")))
  (is (= "andrey.eth" (stateofus/username-with-domain "andrey.eth")))
  (is (= "andrey.stateofus.eth" (stateofus/username-with-domain "andrey.stateofus.eth"))))
