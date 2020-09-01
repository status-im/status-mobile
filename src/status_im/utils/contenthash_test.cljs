(ns status-im.utils.contenthash-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.contenthash :as contenthash]))

;; we reuse the exemple from EIP-1577 but omit content type: dag-pb (0x70)
;; in the contenthash as we do not support it yet

(deftest contenthash-decode
  (testing "decoding a valid ipfs hash"
    (is (= (contenthash/decode "0xe3010170122029f2d17be6139079dc48696d1f582a8530eb9805b561eda517e22a892c7e3f1f")
           {:namespace :ipfs
            :hash "bafybeibj6lixxzqtsb45ysdjnupvqkufgdvzqbnvmhw2kf7cfkesy7r7d4"})))
  (testing "decoding a valid ipns hash"
    (is (= (contenthash/decode "0xe5010170000f6170702e756e69737761702e6f7267")
           {:namespace :ipns
            :hash "app.uniswap.org"})))
  (testing "decoding a valid swarm hash"
    (is (= (contenthash/decode "0xe40101fa011b20d1de9994b4d039f6548d191eb26786769f580809256b4685ef316805265ea162")
           {:namespace :swarm
            :hash "d1de9994b4d039f6548d191eb26786769f580809256b4685ef316805265ea162"})))
  (testing "decoding an invalid ipfs hash"
    (is (nil? (contenthash/decode "0xe301122029f2d17be6139079dc48696d1f582a8530eb9805b561eda517e2"))))
  (testing "decoding random garbage"
    (is (nil? (contenthash/decode "0xabcdef1234567890"))))
  (testing "decoding nil"
    (is (nil? (contenthash/decode nil)))))