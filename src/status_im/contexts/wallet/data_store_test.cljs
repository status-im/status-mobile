(ns status-im.contexts.wallet.data-store-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [matcher-combinators.matchers :as matchers]
    matcher-combinators.test
    [status-im.contexts.wallet.data-store :as sut]
    [utils.collection :as utils.collection]))

(def raw-account
  {:path                  "m/44'/60'/0'/0/0"
   :name                  "Account name"
   :wallet                true
   :chat                  false
   :emoji                 "🍙"
   :colorId               "blue"
   :type                  "generated"
   :createdAt             1716548742000
   :prodPreferredChainIds "1:42161"
   :testPreferredChainIds "11155111:421614"
   :removed               false
   :operable              "fully"})

(defn make-raw-account
  [overrides]
  (merge raw-account overrides))

(def raw-keypair-profile
  {:type     "profile"
   :key-uid  "0x000"
   :accounts [(make-raw-account {:key-uid "0x000"
                                 :address "1x000"
                                 :wallet  false
                                 :chat    true})
              (make-raw-account {:key-uid "0x000"
                                 :address "2x000"})]})

(def raw-keypair-seed-phrase
  {:type     "seed"
   :key-uid  "0x123"
   :accounts [(make-raw-account {:key-uid "0x123"
                                 :address "1x123"})]})

(def raw-keypair-private-key
  {:type     "key"
   :key-uid  "0x456"
   :accounts [(make-raw-account {:key-uid  "0x456"
                                 :address  "1x456"
                                 :operable "no"})]})

(deftest chain-ids-string->set-test
  (testing "chaind-ids-string->set splits and parses chain-ids from string"
    (is (match? #{1 42161}
                (sut/chain-ids-string->set (:prodPreferredChainIds raw-account))))
    (is (match? #{11155111 421614}
                (sut/chain-ids-string->set (:testPreferredChainIds raw-account))))))

(deftest rpc->keypair-test
  (testing "rpc->keypair transforms a profile keypair"
    (is
     (match? {:type               :profile
              :key-uid            "0x000"
              :lowest-operability :fully
              :accounts           [{:key-uid                  "0x000"
                                    :operable                 :fully
                                    :chat                     true
                                    :wallet                   false
                                    :address                  "1x000"
                                    :path                     (:path raw-account)
                                    :name                     (:name raw-account)
                                    :removed                  (:removed raw-account)
                                    :color                    (keyword (:colorId raw-account))
                                    :created-at               (:createdAt raw-account)
                                    :prod-preferred-chain-ids (sut/chain-ids-string->set
                                                               (:prodPreferredChainIds raw-account))
                                    :test-preferred-chain-ids (sut/chain-ids-string->set
                                                               (:testPreferredChainIds raw-account))}
                                   {:key-uid                  "0x000"
                                    :operable                 :fully
                                    :chat                     false
                                    :wallet                   true
                                    :address                  "2x000"
                                    :path                     (:path raw-account)
                                    :name                     (:name raw-account)
                                    :removed                  (:removed raw-account)
                                    :color                    (keyword (:colorId raw-account))
                                    :created-at               (:createdAt raw-account)
                                    :prod-preferred-chain-ids (sut/chain-ids-string->set
                                                               (:prodPreferredChainIds raw-account))
                                    :test-preferred-chain-ids (sut/chain-ids-string->set
                                                               (:testPreferredChainIds raw-account))}]}
             (sut/rpc->keypair raw-keypair-profile))))
  (testing "rpc->keypair transforms a seed-phrase keypair"
    (is
     (match? {:type               :seed
              :key-uid            "0x123"
              :lowest-operability :fully
              :accounts           [{:key-uid                  "0x123"
                                    :address                  "1x123"
                                    :operable                 :fully
                                    :path                     (:path raw-account)
                                    :name                     (:name raw-account)
                                    :wallet                   (:wallet raw-account)
                                    :removed                  (:removed raw-account)
                                    :color                    (keyword (:colorId raw-account))
                                    :created-at               (:createdAt raw-account)
                                    :prod-preferred-chain-ids (sut/chain-ids-string->set
                                                               (:prodPreferredChainIds raw-account))
                                    :test-preferred-chain-ids (sut/chain-ids-string->set
                                                               (:testPreferredChainIds raw-account))}]}
             (sut/rpc->keypair raw-keypair-seed-phrase))))
  (testing "rpc->keypair transforms a raw private-key keypair with inoperable accounts"
    (is
     (match? {:type               :key
              :key-uid            "0x456"
              :lowest-operability :no
              :accounts           [{:key-uid                  "0x456"
                                    :address                  "1x456"
                                    :operable                 :no
                                    :path                     (:path raw-account)
                                    :name                     (:name raw-account)
                                    :wallet                   (:wallet raw-account)
                                    :removed                  (:removed raw-account)
                                    :color                    (keyword (:colorId raw-account))
                                    :created-at               (:createdAt raw-account)
                                    :prod-preferred-chain-ids (sut/chain-ids-string->set
                                                               (:prodPreferredChainIds raw-account))
                                    :test-preferred-chain-ids (sut/chain-ids-string->set
                                                               (:testPreferredChainIds
                                                                raw-account))}]}
             (sut/rpc->keypair raw-keypair-private-key)))))

(deftest rpc->keypairs-test
  (testing "rpc->keypairs transforms and sorts raw keypairs"
    (is
     (match? [(sut/rpc->keypair raw-keypair-profile)
              (sut/rpc->keypair raw-keypair-seed-phrase)
              (sut/rpc->keypair raw-keypair-private-key)]
             (sut/rpc->keypairs [raw-keypair-seed-phrase
                                 raw-keypair-private-key
                                 raw-keypair-profile])))))

(deftest process-keypairs-test
  (testing "process-keypairs represents updated key pairs and accounts"
    (is
     (match?
      (matchers/match-with
       [set? matchers/set-equals
        map? matchers/equals]
       {:removed-keypair-ids         #{}
        :removed-account-addresses   #{}
        :updated-accounts-by-address (->>
                                       [raw-keypair-seed-phrase
                                        raw-keypair-private-key]
                                       (sut/rpc->keypairs)
                                       (mapcat :accounts)
                                       (utils.collection/index-by :address))
        :updated-keypairs-by-id      {(:key-uid raw-keypair-seed-phrase)
                                      (sut/rpc->keypair raw-keypair-seed-phrase)
                                      (:key-uid raw-keypair-private-key)
                                      (sut/rpc->keypair raw-keypair-private-key)}})
      (sut/process-keypairs [raw-keypair-seed-phrase
                             raw-keypair-private-key]))))
  (testing "process-keypairs represents removed key pairs and accounts"
    (is (match?
         (matchers/match-with
          [set? matchers/set-equals
           map? matchers/equals]
          {:removed-keypair-ids         #{"0x456"}
           :removed-account-addresses   #{"1x456"}
           :updated-accounts-by-address (->> [raw-keypair-seed-phrase]
                                             (sut/rpc->keypairs)
                                             (mapcat :accounts)
                                             (utils.collection/index-by :address))
           :updated-keypairs-by-id      {(:key-uid raw-keypair-seed-phrase)
                                         (sut/rpc->keypair raw-keypair-seed-phrase)}})
         (sut/process-keypairs [raw-keypair-seed-phrase
                                (assoc raw-keypair-private-key :removed true)]))))
  (testing "process-keypairs ignores chat accounts inside updated accounts"
    (is
     (match?
      (matchers/match-with
       [set? matchers/set-equals
        map? matchers/equals]
       {:removed-keypair-ids         #{}
        :removed-account-addresses   #{}
        :updated-accounts-by-address (->> [raw-keypair-profile]
                                          (sut/rpc->keypairs)
                                          (mapcat :accounts)
                                          (filter (comp not :chat))
                                          (utils.collection/index-by :address))
        :updated-keypairs-by-id      {(:key-uid raw-keypair-profile)
                                      (sut/rpc->keypair raw-keypair-profile)}})
      (sut/process-keypairs [raw-keypair-profile])))))
