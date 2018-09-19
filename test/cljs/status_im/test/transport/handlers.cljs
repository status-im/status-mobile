(ns status-im.test.transport.handlers
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.transport.handlers :as handlers]))

(def sig "0x04325367620ae20dd878dbb39f69f02c567d789dd21af8a88623dc5b529827c2812571c380a2cd8236a2851b8843d6486481166c39debf60a5d30b9099c66213e4")

(def messages #js [{:sig       sig
                    :ttl       10
                    :timestamp 1527692015
                    :topic     "0x9c22ff5f"
                    :payload   "0x5b227e236334222c5b2246222c22746578742f706c61696e222c227e3a7075626c69632d67726f75702d757365722d6d657373616765222c3135323736393230313433383130312c313532373639323031343337375d5d"
                    :padding   "0xbf06347cc7f9aa18b4a846032264a88f559d9b14079975d14b10648847c0543a77a80624e101c082d19b502ae3b4f97958d18abf59eb0a82afc1301aa22470495fac739a30c2f563599fa8d8e09363a43d39311596b7f119dee7b046989c08224f1ef5cdc385"
                    :pow       0.002631578947368421
                    :hash      "0x220ef9994a4fae64c112b27ed07ef910918159cbe6fcf8ac515ee2bf9a6711a0"}])

(deftest receive-whisper-messages-test
  (testing "an error is reported"
    (is (nil? (handlers/receive-whisper-messages {:db {}} [nil "error" #js [] nil]))))
  (testing "messages is undefined"
    (is (nil? (handlers/receive-whisper-messages {:db {}} [nil nil js/undefined nil]))))
  (testing "happy path"
    (let [actual (handlers/receive-whisper-messages {:db {}} [nil nil messages sig])]
      (testing "it add an fx for the message"
        (is (:chat-received-message/add-fx actual))))))
