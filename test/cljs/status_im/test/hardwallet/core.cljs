(ns status-im.test.hardwallet.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.hardwallet.core :as hardwallet]))

(deftest process-pin-input
  (testing "start entering PIN"
    (is (= {:db {:hardwallet {:pin {:original     [1]
                                    :confirmation []
                                    :status       nil
                                    :enter-step   :original}}}}
           (hardwallet/process-pin-input {:db {:hardwallet {:pin {:original     [1]
                                                                  :confirmation []
                                                                  :enter-step   :original}}}}))))
  (testing "first 6 numbers entered"
    (is (= {:db {:hardwallet {:pin {:original     [1 2 3 4 5 6]
                                    :confirmation []
                                    :status       nil
                                    :enter-step   :confirmation}}}}
           (hardwallet/process-pin-input {:db {:hardwallet {:pin {:original     [1 2 3 4 5 6]
                                                                  :confirmation []
                                                                  :enter-step   :original}}}}))))
  (testing "confirmation entered"
    (is (= {:db                    {:hardwallet {:pin             {:original     [1 2 3 4 5 6]
                                                                   :confirmation [1 2 3 4 5 6]
                                                                   :current      [1 1 1 1 1 1]
                                                                   :enter-step   :confirmation
                                                                   :status       :verifying}
                                                 :card-connected? true}}
            :hardwallet/change-pin {:new-pin     "123456"
                                    :current-pin "111111"
                                    :pairing     nil}}
           (hardwallet/process-pin-input {:db {:hardwallet {:pin             {:original     [1 2 3 4 5 6]
                                                                              :confirmation [1 2 3 4 5 6]
                                                                              :current      [1 1 1 1 1 1]
                                                                              :enter-step   :confirmation}
                                                            :card-connected? true}}}))))

  (testing "confirmation doesn't match"
    (is (= {:db {:hardwallet {:pin {:original     []
                                    :confirmation []
                                    :enter-step   :original
                                    :error-label  :t/pin-mismatch
                                    :status       :error}}}}
           (hardwallet/process-pin-input {:db {:hardwallet {:pin {:original     [1 2 3 4 5 6]
                                                                  :confirmation [1 2 3 4 5 7]
                                                                  :enter-step   :confirmation}}}})))))

(deftest on-generate-and-load-key-success
  (is (= (select-keys
          (get-in
           (hardwallet/on-generate-and-load-key-success
            {:random-guid-generator (constantly "")
             :signing-phrase        ""
             :status                ""
             :db                    {}}
            #js {"whisper-private-key"   "f342f3ef17ce86abfa92b912b3a108a4546cfc687fd8e0f02fedf87a60ee4c0f"
                 "whisper-public-key"    "04de2e21f1642ebee03b9aa4bf1936066124cc89967eaf269544c9b90c539fd5c980166a897d06dd4d3732b38116239f63c89395a8d73eac72881fab802010cb56"
                 "encryption-public-key" "04f2a432677a1b7c4f1bb22078135821d1d10fce23422297b5c808a545f2b61cdba38ee7394762172fc6ff5e9e28db7535e555efe2812905ffd4e0c25e82a98ae8"
                 "whisper-address"       "87df2285f90b71221fab6267b7cb37532fedbb1f"
                 "wallet-address"        "7e92236392a850980d00d0cd2a4b92886bd7fe7b"})
           [:db :hardwallet :multiaccount])
          [:whisper-private-key
           :whisper-public-key
           :encryption-public-key
           :wallet-address
           :whisper-address]))
      {:whisper-private-key   "f342f3ef17ce86abfa92b912b3a108a4546cfc687fd8e0f02fedf87a60ee4c0f"
       :whisper-public-key    "0x04de2e21f1642ebee03b9aa4bf1936066124cc89967eaf269544c9b90c539fd5c980166a897d06dd4d3732b38116239f63c89395a8d73eac72881fab802010cb56"
       :encryption-public-key "04f2a432677a1b7c4f1bb22078135821d1d10fce23422297b5c808a545f2b61cdba38ee7394762172fc6ff5e9e28db7535e555efe2812905ffd4e0c25e82a98ae8"
       :whisper-address       "87df2285f90b71221fab6267b7cb37532fedbb1f"
       :wallet-address        "7e92236392a850980d00d0cd2a4b92886bd7fe7b"}))
