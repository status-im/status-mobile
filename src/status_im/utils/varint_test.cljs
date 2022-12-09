(ns status-im.utils.varint-test
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.utils.varint :as varint]))

(deftest encode
  (is (= (varint/encode-hex 0x0)
         "00"))
  (is (= (varint/encode-hex 0x70)
         "70"))
  (is (= (varint/encode-hex 0xe3)
         "e301")))

(deftest decode
  (is (= (varint/decode-hex "00")
         0x0))
  (is (= (varint/decode-hex "70")
         0x70))
  (is (= (varint/decode-hex "e301")
         0xe3)))

(defn test-roundtrip [n]
  (= (varint/decode-hex (varint/encode-hex n))
     n))

(deftest roundtrip
  (is (test-roundtrip 0))
  (is (test-roundtrip 23948))
  (is (test-roundtrip 2684453)))
