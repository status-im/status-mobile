(ns status-im.test.wallet.transactions
  (:require [cljs.test :refer-macros [deftest is testing async run-tests]]
            [cljs.core.async.impl.protocols :refer [closed?]]
            [status-im.utils.datetime :as time]
            [status-im.utils.http :as http]
            [status-im.models.transactions :as transactions]
            [goog.Uri :as goog-uri]))

(deftest have-unconfirmed-transactions
  (is (transactions/have-unconfirmed-transactions?
       [{:confirmations "0"}]))
  (is (transactions/have-unconfirmed-transactions?
       [{:confirmations "11"}]))
  (is (transactions/have-unconfirmed-transactions?
       [{:confirmations "200"}
        {:confirmations "0"}]))
  (is (not (transactions/have-unconfirmed-transactions?
            [{:confirmations "12"}]))))

(deftest chat-map->transaction-ids
  (is (= #{} (transactions/chat-map->transaction-ids "testnet_rpc" {})))
  (is (= #{"a" "b" "c" "d"}
         (transactions/chat-map->transaction-ids
          "testnet_rpc"
          {:a {:messages {1 {:content-type "command"
                             :content {:params {:tx-hash "a"
                                                :network "testnet"}}}}}
           :b {:messages {1 {:content-type "command"
                             :content {:params {:tx-hash "b"
                                                :network "testnet"}}}}}
           :c {:messages {1 {:content-type "command"
                             :content {:params {:tx-hash "c"
                                                :network "testnet"}}}
                          2 {:content-type "command"
                             :content {:params {:tx-hash "d"
                                                :network "testnet"}}}}}})))

  (is (= #{"a" "b" "c" "d" "e"}
         (transactions/chat-map->transaction-ids
          "testnet"
          {:aa {:messages {1 {:content-type "command"
                              :content {:params {:tx-hash "a"
                                                 :network "testnet"}}}}}
           :bb {:messages {1 {:content-type "command"
                              :content {:params {:tx-hash "b"
                                                 :network "testnet"}}}}}
           :cc {:messages {1 {:content-type "command"
                              :content {:params {:tx-hash "c"
                                                 :network "testnet"}}}
                           2 {:content-type "command"
                              :content {:params {:tx-hash "d"
                                                 :network "testnet"}}}
                           3 {:content-type "command"
                              :content {:params {:tx-hash "e"
                                                 :network "testnet"}}}}}})))
  (is (= #{"b"}
         (transactions/chat-map->transaction-ids
          "testnet_rpc"
          {:aa {:public? true
                :messages {1 {:content-type "command"
                              :content {:params {:tx-hash "a"
                                                 :network "testnet"}}}}}
           :bb {:messages {1 {:content-type "command"
                              :content {:params {:tx-hash "b"
                                                 :network "testnet"}}}}}
           :cc {:messages {1 {:content {:params {:tx-hash "c"
                                                 :network "testnet"}}}
                           2 {:content-type "command"}}}}))))

;; The following tests are fantastic for developing the async-periodic-exec
;; but dismal for CI because of their probablistic nature
#_(deftest async-periodic-exec
    (testing "work-fn is executed and can be stopeed"
      (let [executor (atom nil)
            state (atom 0)]
        (reset! executor
                (transactions/async-periodic-exec
                 (fn [done-fn]
                   (swap! state inc)
                   (done-fn))
                 100
                 500))
        (async test-done
               (js/setTimeout
                (fn []
                  (is (> 6 @state 2))
                  (transactions/async-periodic-stop! @executor)
                  (let [st @state]
                    (js/setTimeout
                     #(do
                        (is (= st @state))
                        (is (closed? @executor))
                        (test-done))
                     500)))
                500)))))

#_(deftest async-periodic-exec-error-in-job
    (testing "error thrown in job is caught and loop continues"
      (let [executor (atom nil)
            state (atom 0)]
        (reset! executor
                (transactions/async-periodic-exec
                 (fn [done-fn]
                   (swap! state inc)
                   (throw (ex-info "Throwing this on purpose in error-in-job test" {})))
                 10
                 100))
        (async test-done
               (js/setTimeout
                (fn []
                  (is (> @state 1))
                  (transactions/async-periodic-stop! @executor)
                  (let [st @state]
                    (js/setTimeout
                     #(do
                        (is (= st @state))
                        (is (closed? @executor))
                        (test-done))
                     500)))
                1000)))))

#_(deftest async-periodic-exec-job-takes-longer
    (testing "job takes longer than expected, executor timeout but task side-effects are still applied"
      (let [executor (atom nil)
            state (atom 0)]
        (reset! executor
                (transactions/async-periodic-exec
                 (fn [done-fn] (js/setTimeout #(swap! state inc) 100))
                 10
                 1))
        (async test-done
               (js/setTimeout
                (fn []
                  (transactions/async-periodic-stop! @executor)
                  (js/setTimeout
                   #(do (is (< 3 @state))
                        (test-done))
                   500))
                500)))))

#_(deftest async-periodic-exec-stop-early
    (testing "stopping early prevents any executions"
      (let [executor (atom nil)
            state (atom 0)]
        (reset! executor
                (transactions/async-periodic-exec
                 (fn [done-fn]
                   (swap! state inc)
                   (done-fn))
                 100
                 100))
        (async test-done
               (js/setTimeout
                (fn []
                  (is (zero? @state))
                  (transactions/async-periodic-stop! @executor)
                  (let [st @state]
                    (js/setTimeout
                     (fn []
                       (is (zero? @state))
                       (test-done))
                     500)))
                50)))))

(defn- uri-query-data [uri]
  (let [uri' (goog-uri/parse uri)
        accum (atom {})]
    (.forEach (.getQueryData uri')
              #(swap! accum assoc (keyword %2) %1))
    {:scheme (.getScheme uri')
     :domain (.getDomain uri')
     :path (.getPath uri')
     :query @accum}))

(deftest get-transaction-details-url
  (is (= "https://etherscan.io/tx/asdfasdf"
         (transactions/get-transaction-details-url :mainnet "asdfasdf")))
  (is (= "https://rinkeby.etherscan.io/tx/asdfasdfg"
         (transactions/get-transaction-details-url :rinkeby "asdfasdfg")))
  (is (= "https://ropsten.etherscan.io/tx/asdfasdfgg"
         (transactions/get-transaction-details-url :testnet "asdfasdfgg")))
  (is (nil? (transactions/get-transaction-details-url :not-a-net "asdfasdfg")))
  (is (thrown? js/Error (transactions/get-transaction-details-url nil "asdfasdfg")))
  (is (thrown? js/Error (transactions/get-transaction-details-url :asdf 1))))

(deftest get-transaction-url
  (is (= {:scheme "https",
          :domain "api.etherscan.io",
          :path "/api",
          :query
          {:module "account",
           :action "txlist",
           :address "0xasdfasdf",
           :startblock "0",
           :endblock "99999999",
           :sort "desc",
           :apikey "DMSI4UAAKUBVGCDMVP3H2STAMSAUV7BYFI",
           :q "json"}}
         (uri-query-data (transactions/get-transaction-url :mainnet "asdfasdf"))))
  (is (= {:scheme "https",
          :domain "api-rinkeby.etherscan.io",
          :path "/api",
          :query
          {:module "account",
           :action "txlist",
           :address "0xasdfasdfg",
           :startblock "0",
           :endblock "99999999",
           :sort "desc",
           :apikey "DMSI4UAAKUBVGCDMVP3H2STAMSAUV7BYFI",
           :q "json"}}
         (uri-query-data (transactions/get-transaction-url :rinkeby "asdfasdfg"))))
  (let [uri (-> (transactions/get-transaction-url :testnet "asdfasdfgg")
                uri-query-data)]
    (is (= "api-ropsten.etherscan.io" (:domain uri)))
    (is (= "0xasdfasdfgg" (-> uri :query :address))))
  (is (thrown? js/Error (transactions/get-transaction-url nil "asdfasdfg"))))

(declare  mock-etherscan-success-response
          mock-etherscan-error-response
          mock-etherscan-empty-response)

(deftest etherscan-transactions
  (let [ky-set #{:block :hash :symbol :gas-price :value :gas-limit :type
                 :confirmations :gas-used :from :timestamp :nonce :to :data}]
    (with-redefs [http/get (fn [url success-fn error-fn]
                             (success-fn mock-etherscan-success-response))]
      (let [result (atom nil)]
        (transactions/etherscan-transactions
         :mainnet
         "asdfasdf"
         #(reset! result %)
         (fn [er]))
        (doseq [[tx-hash tx-map] @result]
          (is (string? tx-hash))
          (is (= tx-hash (:hash tx-map)))
          (is (keyword (:symbol tx-map)))
          (is (= :outbound (:type tx-map)))
          (is (every? identity
                      (map (partial get tx-map)
                           ky-set)))
          (is (= (set (keys tx-map))
                 ky-set))))))

  (with-redefs [http/get (fn [url success-fn error-fn]
                           (success-fn mock-etherscan-empty-response))]
    (let [result (atom nil)]
      (transactions/etherscan-transactions
       :mainnet
       "asdfasdf"
       #(reset! result %)
       (fn [er]))
      (is (= {} @result))))

  (with-redefs [http/get (fn [url success-fn error-fn]
                           (success-fn mock-etherscan-error-response))]
    (let [result (atom nil)]
      (transactions/etherscan-transactions
       :mainnet
       "asdfasdf"
       #(reset! result %)
       (fn [er]))
      (is (= {} @result)))))

#_(run-tests)

(def mock-etherscan-error-response
  "{\"status\":\"0\",\"message\":\"NOTOK\",\"result\":\"Error!\"}")

(def mock-etherscan-empty-response
  "{\"status\":\"0\",\"message\":\"No transactions found\",\"result\":[]}")

(def mock-etherscan-success-response
  "{\"status\":\"1\",\"message\":\"OK\",\"result\":[{\"blockNumber\":\"6662956\",\"timeStamp\":\"1541632935\",\"hash\":\"0x5899677055f3e4939b3878dc2dbc71b79cd6c2871a3aef61db25bad24c113258\",\"nonce\":\"3\",\"blockHash\":\"0x088411b630edf04bfc57dce6b70faf54dff917e1014aab7d703c3652acf7db1f\",\"transactionIndex\":\"92\",\"from\":\"0xa19536ed80c2a37e6925002e36c28de9c08737d3\",\"to\":\"0x744d70fdbe2ba4cf95131626614a1763df805b9e\",\"value\":\"0\",\"gas\":\"105000\",\"gasPrice\":\"3000000000\",\"isError\":\"0\",\"txreceipt_status\":\"1\",\"input\":\"0xa9059cbb000000000000000000000000e829f7947175fe6a338344e70aa770a8c134372c0000000000000000000000000000000000000000000000000de0b6b3a7640000\",\"contractAddress\":\"\",\"cumulativeGasUsed\":\"7482036\",\"gasUsed\":\"91130\",\"confirmations\":\"5334\"},{\"blockNumber\":\"6662619\",\"timeStamp\":\"1541628439\",\"hash\":\"0x3f2caac716c9bbd65ee76afa1df8e4ba3a1b3a15419316bdb31388bc5ad108b2\",\"nonce\":\"2\",\"blockHash\":\"0xff4c76a65c63ae4cb82e56dc94a4fb5c05aa9879ed1f3abbb0c103931613dbb2\",\"transactionIndex\":\"159\",\"from\":\"0xa19536ed80c2a37e6925002e36c28de9c08737d3\",\"to\":\"0x744d70fdbe2ba4cf95131626614a1763df805b9e\",\"value\":\"0\",\"gas\":\"105000\",\"gasPrice\":\"7260000000\",\"isError\":\"0\",\"txreceipt_status\":\"1\",\"input\":\"0xa9059cbb000000000000000000000000e829f7947175fe6a338344e70aa770a8c134372c0000000000000000000000000000000000000000000000000de0b6b3a7640000\",\"contractAddress\":\"\",\"cumulativeGasUsed\":\"6013584\",\"gasUsed\":\"91130\",\"confirmations\":\"5671\"},{\"blockNumber\":\"6606087\",\"timeStamp\":\"1540827293\",\"hash\":\"0x3ceecf0fc3cf5ef21081da06f4bf2e19dcfdfbb0672f529a1c56d791ad89c3d6\",\"nonce\":\"1\",\"blockHash\":\"0x5a4cd35c3a6cac19addb0e7e407b0fcdf33a16fc93e61442bc8fe494a9f8ccb9\",\"transactionIndex\":\"34\",\"from\":\"0xa19536ed80c2a37e6925002e36c28de9c08737d3\",\"to\":\"0x167c7c3d434315e4415eb802f0beb9ea44cd1546\",\"value\":\"0\",\"gas\":\"1500058\",\"gasPrice\":\"5000000000\",\"isError\":\"0\",\"txreceipt_status\":\"1\",\"input\":\"0x7055d368000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000052000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000de0b6b3a76400000000000000000000000000000000000000000000000000000de0b6b3a764000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003782dace9d900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000de0b6b3a76400000000000000000000000000000000000000000000000000000de0b6b3a764000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003782dace9d90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000007ce66c50e28400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000015af1d78b58c4000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003782dace9d90000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000007ce66c50e28400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001f399b1438a1000000000000000000000000000000000000000000000000000003782dace9d900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\",\"contractAddress\":\"\",\"cumulativeGasUsed\":\"5272966\",\"gasUsed\":\"1400058\",\"confirmations\":\"62203\"},{\"blockNumber\":\"6600328\",\"timeStamp\":\"1540745424\",\"hash\":\"0x138ca44da0062fb35e391e18f2728f383848c92cf8a4181484006a49729f7047\",\"nonce\":\"115\",\"blockHash\":\"0x3a72a3fa8f19f5d4d1f37531f7d6fb4e2ac8cbac41660dc40221b1625031e7af\",\"transactionIndex\":\"28\",\"from\":\"0xb81791767a4a4ba1b48d57872212132f000f98c9\",\"to\":\"0xa19536ed80c2a37e6925002e36c28de9c08737d3\",\"value\":\"25000000000000000\",\"gas\":\"50000\",\"gasPrice\":\"10000000000\",\"isError\":\"0\",\"txreceipt_status\":\"1\",\"input\":\"0x\",\"contractAddress\":\"\",\"cumulativeGasUsed\":\"1364725\",\"gasUsed\":\"21000\",\"confirmations\":\"67962\"},{\"blockNumber\":\"6582343\",\"timeStamp\":\"1540491006\",\"hash\":\"0x0a18798662544c4988ad914eca56af93326a6ba94be64263d823916f6eb0b032\",\"nonce\":\"0\",\"blockHash\":\"0x54d3c496cdea962adcf9fb924ba77ee116e0d0f3249a7c484090ef937cedcac8\",\"transactionIndex\":\"71\",\"from\":\"0xa19536ed80c2a37e6925002e36c28de9c08737d3\",\"to\":\"0x744d70fdbe2ba4cf95131626614a1763df805b9e\",\"value\":\"0\",\"gas\":\"703286\",\"gasPrice\":\"8000000000\",\"isError\":\"0\",\"txreceipt_status\":\"1\",\"input\":\"0xcae9ca51000000000000000000000000db5ac1a559b02e12f29fc0ec0e37be8e046def490000000000000000000000000000000000000000000000008ac7230489e8000000000000000000000000000000000000000000000000000000000000000000600000000000000000000000000000000000000000000000000000000000000084b82fedbb864f2278d86348afb072d601c38419f882977f16ff5c4152359b7b5013bfd533000000000000000000000000a19536ed80c2a37e6925002e36c28de9c08737d3850ed9944dec60f189d04f34a0a2079c1c8c3d6a3ced837df0667f0d2e62a28128635b33d56041ba7f1fae254df20a2378f096a739e2e2871327c9c177fd0a2d00000000000000000000000000000000000000000000000000000000\",\"contractAddress\":\"\",\"cumulativeGasUsed\":\"5085753\",\"gasUsed\":\"332228\",\"confirmations\":\"85947\"},{\"blockNumber\":\"6500995\",\"timeStamp\":\"1539344556\",\"hash\":\"0x2184fa04f3fc32f942b851c7e15d1fb5997e8c6c6b5871e110529cecb75f8972\",\"nonce\":\"16\",\"blockHash\":\"0x2266dc4e76c06fbf16b701bdac9269cec043ad9d1368166b584aaf2ec6fb5f68\",\"transactionIndex\":\"34\",\"from\":\"0x7b80e03797009f4c545cfb85407931272e34962b\",\"to\":\"0xa19536ed80c2a37e6925002e36c28de9c08737d3\",\"value\":\"50000000000000000\",\"gas\":\"21000\",\"gasPrice\":\"3000000000\",\"isError\":\"0\",\"txreceipt_status\":\"1\",\"input\":\"0x\",\"contractAddress\":\"\",\"cumulativeGasUsed\":\"7854654\",\"gasUsed\":\"21000\",\"confirmations\":\"167295\"}]}")
