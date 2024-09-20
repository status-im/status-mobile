(ns status-im.contexts.wallet.tokens.events-test
  (:require
    [cljs.test :refer [deftest is testing]]
    [status-im.contexts.wallet.tokens.events :as tokens.events]))

(def eth
  {:address  "0x0000000000000000000000000000000000000000"
   :chainId  1
   :decimals 18
   :name     "Ether"
   :symbol   "ETH"
   :verified true})

(def snt
  {:address  "0x0000000000000000000000000000000000000001"
   :chainId  1
   :decimals 18
   :name     "Status Network Token"
   :symbol   "SNT"
   :verified true})

(def another-token1
  {:address  "0x0000000000000000000000000000000000000002"
   :chainId  1
   :decimals 18
   :name     "Another Token 1"
   :symbol   "ANOTHER1"
   :verified false})

(def another-token2
  {:address  "0x0000000000000000000000000000000000000000" ; uses the same address as `eth`
   :chainId  1
   :decimals 18
   :name     "Another Token 2"
   :symbol   "ANOTHER1" ;; uses the same symbol as `another-token1`
   :verified false})


(deftest store-token-list-test
  (testing "response contains one list, and `chain-id` is equal to the one that's in the database"
    (let [cofx {:db {:wallet {:networks {:prod [{:chain-id 1}]}}}}
          data [{:name    "native"
                 :source  "native"
                 :version "1.0.0"
                 :tokens  [eth snt]}]]
      (is
       (match?
        (:db (tokens.events/store-token-list cofx [{:data data}]))
        {:wallet {:networks {:prod [{:chain-id 1}]}
                  :tokens   {:sources    [{:name         "native"
                                           :source       "native"
                                           :version      "1.0.0"
                                           :tokens-count 2}]
                             :by-address '({:address      "0x0000000000000000000000000000000000000000"
                                            :decimals     18
                                            :key          "1-0x0000000000000000000000000000000000000000"
                                            :community-id nil
                                            :symbol       "ETH"
                                            :sources      ["native"]
                                            :name         "Ether"
                                            :type         :erc20
                                            :verified?    true
                                            :chain-id     1
                                            :image        nil}
                                           {:address      "0x0000000000000000000000000000000000000001"
                                            :decimals     18
                                            :key          "1-0x0000000000000000000000000000000000000001"
                                            :community-id nil
                                            :symbol       "SNT"
                                            :sources      ["native"]
                                            :name         "Status Network Token"
                                            :type         :erc20
                                            :verified?    true
                                            :chain-id     1
                                            :image        nil})
                             :by-symbol  '({:address      "0x0000000000000000000000000000000000000000"
                                            :decimals     18
                                            :key          "ETH"
                                            :community-id nil
                                            :symbol       "ETH"
                                            :sources      ["native"]
                                            :name         "Ether"
                                            :type         :erc20
                                            :verified?    true
                                            :chain-id     1
                                            :image        nil}
                                           {:address      "0x0000000000000000000000000000000000000001"
                                            :decimals     18
                                            :key          "SNT"
                                            :community-id nil
                                            :symbol       "SNT"
                                            :sources      ["native"]
                                            :name         "Status Network Token"
                                            :type         :erc20
                                            :verified?    true
                                            :chain-id     1
                                            :image        nil})}
                  :ui       {:loading {:token-list    false
                                       :market-values true
                                       :details       true
                                       :prices        true}}}}))))
  (testing "response contains one list, and `chain-id` is NOT equal to the one that's in the database"
    (let [cofx {:db {:wallet {:networks {:prod [{:chain-id 2}]}}}}
          data [{:name    "native"
                 :source  "native"
                 :version "1.0.0"
                 :tokens  [eth snt]}]]
      (is (match? (-> (tokens.events/store-token-list cofx [{:data data}])
                      :db
                      :wallet
                      :tokens)
                  {:sources    [{:name         "native"
                                 :source       "native"
                                 :version      "1.0.0"
                                 :tokens-count 2}]
                   :by-address nil
                   :by-symbol  nil}))))
  (testing "response contains two lists"
    (let [cofx {:db {:wallet {:networks {:prod [{:chain-id 1}]}}}}
          data [{:name    "native"
                 :source  "native"
                 :version "1.0.0"
                 :tokens  [eth snt]}
                {:name    "second"
                 :source  "second"
                 :version "1.0.0"
                 :tokens  [snt another-token1]}]]
      (is
       (match?
        (-> (tokens.events/store-token-list cofx [{:data data}])
            :db
            :wallet
            :tokens)
        {:sources    [{:name         "native"
                       :source       "native"
                       :version      "1.0.0"
                       :tokens-count 2}
                      {:name         "second"
                       :source       "second"
                       :version      "1.0.0"
                       :tokens-count 2}]
         :by-address '({:address      "0x0000000000000000000000000000000000000000"
                        :decimals     18
                        :key          "1-0x0000000000000000000000000000000000000000"
                        :community-id nil
                        :symbol       "ETH"
                        :sources      ["native"]
                        :name         "Ether"
                        :type         :erc20
                        :verified?    true
                        :chain-id     1
                        :image        nil}
                       {:address      "0x0000000000000000000000000000000000000001"
                        :decimals     18
                        :key          "1-0x0000000000000000000000000000000000000001"
                        :community-id nil
                        :symbol       "SNT"
                        :sources      ["native" "second"]
                        :name         "Status Network Token"
                        :type         :erc20
                        :verified?    true
                        :chain-id     1
                        :image        nil}
                       {:address      "0x0000000000000000000000000000000000000002"
                        :decimals     18
                        :key          "1-0x0000000000000000000000000000000000000002"
                        :community-id nil
                        :symbol       "ANOTHER1"
                        :sources      ["second"]
                        :name         "Another Token 1"
                        :type         :erc20
                        :verified?    false
                        :chain-id     1
                        :image        nil})
         :by-symbol  '({:address      "0x0000000000000000000000000000000000000000"
                        :decimals     18
                        :key          "ETH"
                        :community-id nil
                        :symbol       "ETH"
                        :sources      ["native"]
                        :name         "Ether"
                        :type         :erc20
                        :verified?    true
                        :chain-id     1
                        :image        nil}
                       {:address      "0x0000000000000000000000000000000000000001"
                        :decimals     18
                        :key          "SNT"
                        :community-id nil
                        :symbol       "SNT"
                        :sources      ["native" "second"]
                        :name         "Status Network Token"
                        :type         :erc20
                        :verified?    true
                        :chain-id     1
                        :image        nil}
                       {:address      "0x0000000000000000000000000000000000000002"
                        :decimals     18
                        :key          "ANOTHER1"
                        :community-id nil
                        :symbol       "ANOTHER1"
                        :sources      ["second"]
                        :name         "Another Token 1"
                        :type         :erc20
                        :verified?    false
                        :chain-id     1
                        :image        nil})}))))
  (testing "second list contains a token that replaces the one that was added before"
    (let [cofx {:db {:wallet {:networks {:prod [{:chain-id 1}]}}}}
          data [{:name    "native"
                 :source  "native"
                 :version "1.0.0"
                 :tokens  [eth another-token1]}
                {:name    "second"
                 :source  "second"
                 :version "1.0.0"
                 :tokens  [another-token2]}]]
      (is
       (match?
        (-> (tokens.events/store-token-list cofx [{:data data}])
            :db
            :wallet
            :tokens)
        {:sources    [{:name         "native"
                       :source       "native"
                       :version      "1.0.0"
                       :tokens-count 2}
                      {:name         "second"
                       :source       "second"
                       :version      "1.0.0"
                       :tokens-count 1}]
         :by-address '({:address      "0x0000000000000000000000000000000000000000"
                        :decimals     18
                        :key          "1-0x0000000000000000000000000000000000000000"
                        :community-id nil
                        :symbol       "ANOTHER1"
                        :sources      ["native" "second"]
                        :name         "Another Token 2"
                        :type         :erc20
                        :verified?    false
                        :chain-id     1
                        :image        nil}
                       {:address      "0x0000000000000000000000000000000000000002"
                        :decimals     18
                        :key          "1-0x0000000000000000000000000000000000000002"
                        :community-id nil
                        :symbol       "ANOTHER1"
                        :sources      ["native"]
                        :name         "Another Token 1"
                        :type         :erc20
                        :verified?    false
                        :chain-id     1
                        :image        nil})
         :by-symbol  '({:address      "0x0000000000000000000000000000000000000000"
                        :decimals     18
                        :key          "ETH"
                        :community-id nil
                        :symbol       "ETH"
                        :sources      ["native"]
                        :name         "Ether"
                        :type         :erc20
                        :verified?    true
                        :chain-id     1
                        :image        nil}
                       {:address      "0x0000000000000000000000000000000000000000"
                        :decimals     18
                        :key          "ANOTHER1"
                        :community-id nil
                        :symbol       "ANOTHER1"
                        :sources      ["native" "second"]
                        :name         "Another Token 2"
                        :type         :erc20
                        :verified?    false
                        :chain-id     1
                        :image        nil})})))))
