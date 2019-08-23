(ns status-im.test.tribute-to-talk.core
  (:require [cljs.test :refer-macros [deftest testing is]]
            [status-im.tribute-to-talk.core :as tribute-to-talk]
            [status-im.utils.money :as money]))

(deftest get-new-snt-amount
  (testing "staying under the limit"
    (is (= (tribute-to-talk/get-new-snt-amount
            "999999.9" "9")
           "999999.99")))
  (testing "getting over the limit"
    (is (= (tribute-to-talk/get-new-snt-amount
            "100000" "0")
           "100000")))
  (testing "replace initial 0 by the first digit"
    (is (= (tribute-to-talk/get-new-snt-amount
            "0" "1")
           "1")))
  (testing "disallow more than two dots"
    (is (= (tribute-to-talk/get-new-snt-amount
            "0." ".")
           "0."))
    (is (= (tribute-to-talk/get-new-snt-amount
            "0.0" ".")
           "0.0")))
  (testing "disallow more than two digits after dot"
    (is (= (tribute-to-talk/get-new-snt-amount
            "0.0" "0")
           "0.00"))
    (is (= (tribute-to-talk/get-new-snt-amount
            "0.00" "1")
           "0.00")))
  (testing "0 remains if removed is pressed on last digit"
    (is (= (tribute-to-talk/get-new-snt-amount
            "0" :remove)
           "0"))
    (is (= (tribute-to-talk/get-new-snt-amount
            "1" :remove)
           "0")))
  (testing "dot is removed when last digit after dot is removed"
    (= (tribute-to-talk/get-new-snt-amount
        "1." :remove)
       "1")
    (= (tribute-to-talk/get-new-snt-amount
        "1.1" :remove)
       "1")))

(def recipient-pk "0x04263d74e55775280e75b4a4e9a45ba59fc372793a869c5d9c4fa2100556d9963e3f4fbfa1724ec94a46e6da057540ab248ed1f5eb956e36e3129ecd50fade2c97")
(def recipient-address "0xdff1a5e4e57d9723b3294e0f4413372e3ea9a8ff")

(def user-cofx
  {:db {:multiaccount
        {:address "954d4393515747ea75808a0301fb73317ae1e460"
         :network "testnet_rpc"
         :networks/networks {"testnet_rpc" {:config {:NetworkId 3}}}
         :settings {:tribute-to-talk {:testnet {:snt-amount "1000000000000000000"}}}}
        :contacts/contacts
        {recipient-pk {:name "bob"
                       :address recipient-address
                       :public-key recipient-pk
                       :tribute-to-talk {:snt-amount "1000000000000000000"}}}
        :wallet {:balance {:STT (money/bignumber "1000000000000000000")}}}})

(deftest tribute-transaction-trigger
  (testing "transaction error"
    (is (tribute-to-talk/tribute-transaction-trigger
         {:ethereum/current-block 10}
         {:block "5"
          :error? true})))
  (testing "transaction confirmed"
    (is (tribute-to-talk/tribute-transaction-trigger
         {:ethereum/current-block 10}
         {:block "5"})))
  (testing "transaction not confirmed yet"
    (is (not (tribute-to-talk/tribute-transaction-trigger
              {:ethereum/current-block 5}
              {:block "5"})))))

(def public-key "0xabcdef")
(def my-public-key "0x000001")
(def test-db
  {:chats
   {public-key {:group-chat false}}
   :contacts/contacts
   {public-key {:tribute-to-talk {}
                :system-tags #{}}}
   :multiaccount
   {:public-key my-public-key}
   :networks/current-network "mainnet_rpc"
   :networks/networks {"mainnet_rpc" {:id "mainnet_rpc"
                                      :config {:NetworkId 1}}
                       "testnet_rpc" {:id "testnet_rpc"
                                      :config {:NetworkId 3}}}})

(deftest check-tribute
  (testing "No contract in network, own public key"
    (let [result (tribute-to-talk/check-tribute {:db test-db} my-public-key)]
      (is (= (-> test-db
                 (assoc :navigation/screen-params {:tribute-to-talk {:unavailable? true}})
                 (assoc-in [:multiaccount :settings] {:tribute-to-talk {:mainnet nil}}))
             (:db result)))))

  (testing "No contract in network, another public key"
    (let [result (tribute-to-talk/check-tribute {:db test-db} public-key)]
      (is (= {:disabled? true}
             (get-in result [:db :contacts/contacts public-key :tribute-to-talk])))))

  (testing "Contract in network, another public key"
    (let [result (tribute-to-talk/check-tribute {:db (assoc test-db :networks/current-network "testnet_rpc")}
                                                public-key)]
      (is (= "0xC61aa0287247a0398589a66fCD6146EC0F295432"
             (get-in result [:tribute-to-talk/get-tribute :contract])))))

  (testing "Added by other user"
    (let [result (tribute-to-talk/check-tribute
                  {:db (update-in test-db
                                  [:contacts/contacts public-key :system-tags]
                                  conj :contact/request-received)}
                  public-key)]
      (is (= result nil))))

  (testing "Group chat"
    (let [result (tribute-to-talk/check-tribute
                  {:db (assoc-in test-db [:chats public-key :group-chat] true)}
                  public-key)]
      (is (= result nil))))

  (testing "Added by this user"
    (let [result (tribute-to-talk/check-tribute
                  {:db (update-in test-db
                                  [:contacts/contacts public-key :system-tags]
                                  conj :contact/added)}
                  public-key)]
      (is (= {:disabled? true}
             (get-in result [:db :contacts/contacts public-key :tribute-to-talk]))))))
