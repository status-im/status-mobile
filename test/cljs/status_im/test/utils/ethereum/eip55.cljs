(ns status-im.test.utils.ethereum.eip55
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.ethereum.eip55 :as eip55]))

(deftest valid-address-checksum?
  (is (= true (eip55/valid-address-checksum? "0x52908400098527886E0F7030069857D2E4169EE7")))
  (is (= true (eip55/valid-address-checksum? "0x8617E340B3D01FA5F11F306F4090FD50E238070D")))
  (is (= true (eip55/valid-address-checksum? "0xde709f2102306220921060314715629080e2fb77")))
  (is (= true (eip55/valid-address-checksum? "0x27b1fdb04752bbc536007a920d24acb045561c26")))
  (is (= true (eip55/valid-address-checksum? "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed")))
  (is (= true (eip55/valid-address-checksum? "0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359")))
  (is (= true (eip55/valid-address-checksum? "0xdbF03B407c01E7cD3CBea99509d93f8DDDC8C6FB")))
  (is (= true (eip55/valid-address-checksum? "0xD1220A0cf47c7B9Be7A2E6BA89F429762e7b9aDb")))
  (is (= false (eip55/valid-address-checksum? "0xD1220A0cf47c7B9Be7A2E6BA89F429762e7b9adB")))
  (is (= false (eip55/valid-address-checksum? "0x8617e340b3d01fa5f11f306f4090fd50e238070d")))
  (is (= false (eip55/valid-address-checksum? "0xDE709F2102306220921060314715629080E2fB77"))))