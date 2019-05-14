(ns status-im.test.utils.contenthash
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.contenthash :as contenthash]))

;; we reuse the exemple from EIP-1577 but omit content type: dag-pb (0x70)
;; in the contenthash as we do not support it yet

;; the content hash from EIP-1577 is modified so that to use CIDv1

(deftest contenthash-decode
  (testing "decoding a valid ipfs hash"
    (is (= (contenthash/decode "0xe3010170122029f2d17be6139079dc48696d1f582a8530eb9805b561eda517e22a892c7e3f1f")
           {:namespace :ipfs
            :hash "zdj7WYFeYXcRgTKW6M3DNBQbdNeYd79uQ2yWJD2g1HtWPXeNz"})))
  (testing "decoding an invalid ipfs hash"
    (is (nil? (contenthash/decode "0xe301122029f2d17be6139079dc48696d1f582a8530eb9805b561eda517e2"))))
  (testing "decoding random garbage"
    (is (nil? (contenthash/decode "0xabcdef1234567890"))))
  (testing "decoding nil"
    (is (nil? (contenthash/decode nil)))))

(deftest contenthash-encode
  (testing "encoding a valid ipfs hash"
    (is (= (contenthash/encode
            {:namespace :ipfs
             :hash "QmRAQB6YaCyidP37UdDnjFY5vQuiBrcqdyoW1CuDgwxkD4"})
           "0xe301122029f2d17be6139079dc48696d1f582a8530eb9805b561eda517e22a892c7e3f1f")))
  (testing "encoding a valid ipfs cid"
    (is (= (contenthash/encode
            {:namespace :ipfs
             :hash "zb2rhZfjRh2FHHB2RkHVEvL2vJnCTcu7kwRqgVsf9gpkLgteo"})
           "0xe301015512202cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824")))
  (testing "encoding an invalid ipfs hash"
    (is (nil? (contenthash/encode {:namespace :ipfs
                                   :hash "0xe301122029f2d17be6139079dc48696d1f582a8530eb9805b561eda517e2"}))))
  (testing "encoding random garbage"
    (is (nil? (contenthash/encode {:namespace :ipfs
                                   :hash "0xabcdef1234567890"}))))
  (testing "encoding random garbage"
    (is (nil? (contenthash/encode nil)))))
