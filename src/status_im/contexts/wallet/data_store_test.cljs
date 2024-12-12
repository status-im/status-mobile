(ns status-im.contexts.wallet.data-store-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    matcher-combinators.test
    [status-im.contexts.wallet.data-store :as sut]))

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

(def account
  {:path                     "m/44'/60'/0'/0/0"
   :emoji                    "🍙"
   :color                    :blue
   :wallet                   true
   :default-account?         true
   :name                     "Account name"
   :type                     :generated
   :chat                     false
   :test-preferred-chain-ids #{11155111 421614}
   :watch-only?              false
   :prod-preferred-chain-ids #{1 42161}
   :created-at               1716548742000
   :operable?                true
   :operable                 :fully
   :removed                  false})

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
                                    :emoji                    (:emoji raw-account)
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
                                    :emoji                    (:emoji raw-account)
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
                                    :emoji                    (:emoji raw-account)
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
                                    :emoji                    (:emoji raw-account)
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

(deftest reconcile-keypairs-test
  (testing "reconcile-keypairs represents updated key pairs and accounts"
    (is
     (match-strict?
      {:removed-keypair-ids         #{}
       :removed-account-addresses   #{}
       :updated-accounts-by-address {"1x123" (merge account
                                                    {:key-uid "0x123"
                                                     :address "1x123"})
                                     "1x456" (merge account
                                                    {:key-uid   "0x456"
                                                     :address   "1x456"
                                                     :operable? false
                                                     :operable  :no})}
       :updated-keypairs-by-id      {"0x123" {:key-uid            "0x123"
                                              :type               :seed
                                              :lowest-operability :fully
                                              :accounts           [(merge account
                                                                          {:key-uid "0x123"
                                                                           :address "1x123"})]}
                                     "0x456" {:key-uid            "0x456"
                                              :type               :key
                                              :lowest-operability :no
                                              :accounts           [(merge account
                                                                          {:key-uid   "0x456"
                                                                           :address   "1x456"
                                                                           :operable? false
                                                                           :operable  :no})]}}}
      (sut/reconcile-keypairs [raw-keypair-seed-phrase
                               raw-keypair-private-key]))))
  (testing "reconcile-keypairs represents removed key pairs and accounts"
    (is
     (match-strict?
      {:removed-keypair-ids         #{"0x456"}
       :removed-account-addresses   #{"1x456"}
       :updated-accounts-by-address {"1x123" (merge account
                                                    {:key-uid "0x123"
                                                     :address "1x123"})}
       :updated-keypairs-by-id      {"0x123" {:key-uid            "0x123"
                                              :type               :seed
                                              :lowest-operability :fully
                                              :accounts           [(merge account
                                                                          {:key-uid "0x123"
                                                                           :address "1x123"})]}}}
      (sut/reconcile-keypairs [raw-keypair-seed-phrase
                               (assoc raw-keypair-private-key :removed true)]))))
  (testing "reconcile-keypairs ignores chat accounts inside updated accounts"
    (is
     (match-strict?
      {:removed-keypair-ids         #{}
       :removed-account-addresses   #{}
       :updated-accounts-by-address {"2x000" (merge account
                                                    {:key-uid          "0x000"
                                                     :address          "2x000"
                                                     :chat             false
                                                     :wallet           true
                                                     :default-account? true})}
       :updated-keypairs-by-id      {"0x000" {:key-uid            "0x000"
                                              :type               :profile
                                              :lowest-operability :fully
                                              :accounts           [(merge account
                                                                          {:key-uid          "0x000"
                                                                           :address          "1x000"
                                                                           :chat             true
                                                                           :wallet           false
                                                                           :default-account? false})
                                                                   (merge account
                                                                          {:key-uid          "0x000"
                                                                           :address          "2x000"
                                                                           :chat             false
                                                                           :wallet           true
                                                                           :default-account? true})]}}}
      (sut/reconcile-keypairs [raw-keypair-profile])))))

(def mock-db
  {:wallet {:keypairs
            {"0x123" {:type     "key"
                      :key-uid  "0x123"
                      :keycards [{:id "keycard1"}]}
             "0x456" {:type     "key"
                      :key-uid  "0x456"
                      :keycards []}}
            :ui {:create-account {:selected-keypair-uid "0x123"}}}})

(deftest selected-keypair-keycard?-test
  (testing "returns true when the selected keypair has keycards"
    (is (true? (sut/selected-keypair-keycard? mock-db))))

  (testing "returns false when the selected keypair does not have keycards"
    (let [db (assoc-in mock-db [:wallet :ui :create-account :selected-keypair-uid] "0x456")]
      (is (false? (sut/selected-keypair-keycard? db)))))

  (testing "returns false when the selected keypair does not exist"
    (let [db (assoc-in mock-db [:wallet :ui :create-account :selected-keypair-uid] "0x789")]
      (is (false? (sut/selected-keypair-keycard? db)))))

  (testing "returns false when keypairs map is empty"
    (let [db (assoc-in mock-db [:wallet :keypairs] {})]
      (is (false? (sut/selected-keypair-keycard? db)))))

  (testing "returns false when no keypair is selected"
    (let [db (assoc-in mock-db [:wallet :ui :create-account :selected-keypair-uid] nil)]
      (is (false? (sut/selected-keypair-keycard? db)))))

  (testing "returns false when db does not contain wallet data"
    (let [db {}]
      (is (false? (sut/selected-keypair-keycard? db))))))
