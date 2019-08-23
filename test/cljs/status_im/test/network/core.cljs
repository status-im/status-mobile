(ns status-im.test.network.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.network.core :as network.core]
            [reagent.core :as reagent]))

(deftest valid-rpc-url-test
  (testing "nil?"
    (is (not (network.core/valid-rpc-url? nil))))
  (testing "a blank url"
    (is (not (network.core/valid-rpc-url? ""))))
  (testing "a url without a protocol"
    (is (not (network.core/valid-rpc-url? "something"))))
  (testing "a url without a protocol"
    (is (not (network.core/valid-rpc-url? "http://something with space"))))
  (testing "a url without a hostname"
    (is (not (network.core/valid-rpc-url? "https://"))))
  (testing "an uppercase HTTP url"
    (is (not (network.core/valid-rpc-url? "HTTP://valid.com"))))
  (testing "an http url"
    (is (network.core/valid-rpc-url? "http://valid.com")))
  (testing "an https url"
    (is (network.core/valid-rpc-url? "https://valid.something.else")))
  (testing "a fully qualified url"
    (is (network.core/valid-rpc-url? "https://mainnet.infura.io:6523/v3/f315575765b14720b32382a61a89341a")))
  (testing "an ip address"
    (is (network.core/valid-rpc-url? "https://192.168.1.1")))
  (testing "localhost"
    (is (network.core/valid-rpc-url? "https://localhost")))
  (testing "a fully qualified url, ip address"
    (is (network.core/valid-rpc-url? "https://192.168.1.1:6523/z6GCTmjdP3FETEJmMBI4")))
  (testing "an https url not on the default port"
    (is (network.core/valid-rpc-url? "https://valid.something.else:65323"))))

(deftest new-network-test
  (let [actual (network.core/new-network "randomid"
                                         "network-name"
                                         "upstream-url"
                                         :mainnet
                                         nil)]
    (is (= {:id     "randomid"
            :name   "network-name"
            :config {:NetworkId      1
                     :DataDir        "/ethereum/mainnet_rpc"
                     :UpstreamConfig {:Enabled true
                                      :URL     "upstream-url"}}}
           actual))))

(deftest new-network-id-test
  (let [actual (network.core/new-network "randomid"
                                         "network-name"
                                         "upstream-url"
                                         :mainnet
                                         "5777")]
    (is (= {:id     "randomid"
            :name   "network-name"
            :config {:NetworkId      5777
                     :DataDir        "/ethereum/mainnet_rpc"
                     :UpstreamConfig {:Enabled true
                                      :URL     "upstream-url"}}}
           actual))))

(deftest valid-manage-test
  (testing "a valid manage"
    (is (network.core/valid-manage? {:url   {:value "http://valid.com"}
                                     :name  {:value "valid"}
                                     :chain {:value "valid"}})))
  (testing "invalid url"
    (is (not (network.core/valid-manage? {:url   {:value "invalid"}
                                          :name  {:value "valid"}
                                          :chain {:value "valid"}}))))

  (testing "invalid name"
    (is (not (network.core/valid-manage? {:url   {:value "http://valid.com"}
                                          :name  {:value ""}
                                          :chain {:value "valid"}}))))

  (testing "invalid chain"
    (is (not (network.core/valid-manage? {:url   {:value "http://valid.com"}
                                          :name  {:value "valid"}
                                          :chain {:value ""}})))))

(deftest set-input-test
  (testing "it updates and validate a field"
    (is (= {:db {:networks/manage {:url        {:value "http://valid.com"
                                                :error false}
                                   :name       {:value ""
                                                :error true}
                                   :chain      {:value "mainnet"
                                                :error false}
                                   :network-id {:value nil
                                                :error false}}}}
           (network.core/set-input {:db {:networks/manage {:url   {:value "something"
                                                                   :error true}
                                                           :name  {:value ""
                                                                   :error false}
                                                           :chain {:value "mainnet"
                                                                   :error false}}}}
                                   :url "http://valid.com")))))

(deftest not-save-invalid-url
  (testing "it does not save a network with an invalid url"
    (is (:ui/show-error (network.core/save {:random-id-generator  (constantly "random")
                                            :db {:networks/manage {:url {:value "wrong"}
                                                                   :chain {:value "1"}
                                                                   :name {:value "empty"}}
                                                 :multiaccount {}}})))))

(deftest save-valid-network
  (testing "save a valid network"
    (let [fx (network.core/save {:random-id-generator  (constantly "random-id")
                                 :db {:networks/manage {:url {:value "http://valid.com"}
                                                        :chain {:value :mainnet}
                                                        :name {:value "valid"}}
                                      :multiaccount {}
                                      :networks/networks {"random2"
                                                          {:id     "random2"
                                                           :name   "network-name"
                                                           :config {:NetworkId      1
                                                                    :DataDir        "/ethereum/mainnet_rpc"
                                                                    :UpstreamConfig {:Enabled true
                                                                                     :URL     "upstream-url"}}}}}})]
      (is (= "settings_saveConfig" (:method (first (::json-rpc/call fx)))))
      (is (nil? (:networks/manage (:db fx))))
      (testing "and check that it has an id with `-` and the correct mainnet NetworkId"
        (is (= 1 (get-in fx [:db :networks/networks "randomid" :config :NetworkId])))))))

(deftest not-save-custom-chain-with-non-unique-id
  (testing "it does not save a custom chain with network-id already defined"
    (let [result (network.core/save {:random-id-generator  (constantly "already-defined")
                                     :db {:networks/manage {:url {:value "http://valid.com"}
                                                            :chain {:value :custom}
                                                            :name {:value "valid"}
                                                            :network-id {:value 1}}
                                          :multiaccount {}
                                          :networks/networks {"random"
                                                              {:id     "random"
                                                               :name   "network-name"
                                                               :config {:NetworkId      1
                                                                        :DataDir        "/ethereum/mainnet_rpc"
                                                                        :UpstreamConfig {:Enabled true
                                                                                         :URL     "upstream-url"}}}}}})]
      (is (:ui/show-error result)))))

(deftest save-valid-network-with-unique-chain-id-check
  (testing "save a valid network with chain-id not already defined"
    (let [fx (network.core/save {:random-id-generator  (constantly "random")
                                 :db {:networks/manage {:url {:value "http://valid.com"}
                                                        :chain {:value :mainnet}
                                                        :name {:value "valid"}
                                                        :network-id {:value 5}}
                                      :multiaccount {}
                                      :networks/networks {"randomid"
                                                          {:id     "randomid"
                                                           :name   "network-name"
                                                           :config {:NetworkId      3
                                                                    :DataDir        "/ethereum/mainnet_rpc"
                                                                    :UpstreamConfig {:Enabled true
                                                                                     :URL     "upstream-url"}}}}}})]
      (is (= "settings_saveConfig" (:method (first (::json-rpc/call fx)))))
      (is (nil? (:networks/manage (:db fx))))
      (is (get-in fx [:db :networks/networks "random"])))))
