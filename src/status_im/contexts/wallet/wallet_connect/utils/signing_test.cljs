(ns status-im.contexts.wallet.wallet-connect.utils.signing-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [status-im.contexts.wallet.wallet-connect.utils.signing :as sut]))

(deftest typed-data-chain-id-test
  (testing "chainId is extracted correctly"
    (let [typed-data {:types       {:EIP712Domain [{:name "chainId" :type "string"}]}
                      :domain      {:chainId "1"}
                      :primaryType "EIP712Domain"
                      :message     {}}]
      (is (= (sut/typed-data-chain-id typed-data)
             1))))

  (testing "chainId not extracted if it is not defined in the domain types"
    (let [typed-data {:types       {:EIP712Domain []}
                      :domain      {:chainId "1"}
                      :primaryType "EIP712Domain"
                      :message     {}}]
      (is (nil? (sut/typed-data-chain-id typed-data)))))

  (testing "chainId not extracted if it is not defined in the domain"
    (let [typed-data {:types       {:EIP712Domain [{:name "chainId" :type "string"}]}
                      :domain      {}
                      :primaryType "EIP712Domain"
                      :message     {}}]
      (is (nil? (sut/typed-data-chain-id typed-data)))))

  (testing "chainId not extracted if chainId is not a valid int"
    (let [typed-data {:types       {:EIP712Domain [{:name "chainId" :type "string"}]}
                      :domain      {:chainId "not an int"}
                      :primaryType "EIP712Domain"
                      :message     {}}]
      (is (nil? (sut/typed-data-chain-id typed-data)))))

  (testing "invalid typed data is passed"
    (is (thrown? js/Error (sut/typed-data-chain-id {})))))

(cljs.test/run-tests)
