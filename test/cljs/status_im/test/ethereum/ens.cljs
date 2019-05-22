(ns status-im.test.ethereum.ens
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.ethereum.ens :as ens]))

(deftest namehash
  (is (= (str "0x" ens/default-namehash) (ens/namehash nil)))
  (is (= (str "0x" ens/default-namehash) (ens/namehash "")))
  (is (= "0x93cdeb708b7545dc668eb9280176169d1c33cfd8ed6f04690a0bcc88a93fc4ae"
         (ens/namehash "eth")))
  (is (= "0xde9b09fd7c5f901e23a3f19fecc54828e9c848539801e86591bd9801b019f84f"
         (ens/namehash "foo.eth"))))
