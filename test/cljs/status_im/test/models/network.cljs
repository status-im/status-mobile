(ns status-im.test.models.network
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.network.core :as model]
            [reagent.core :as reagent]))

(deftest valid-rpc-url-test
  (testing "nil?"
    (is (not (model/valid-rpc-url? nil))))
  (testing "a blank url"
    (is (not (model/valid-rpc-url? ""))))
  (testing "a url without a protocol"
    (is (not (model/valid-rpc-url? "something"))))
  (testing "a url without a protocol"
    (is (not (model/valid-rpc-url? "http://something with space"))))
  (testing "a url without a hostname"
    (is (not (model/valid-rpc-url? "https://"))))
  (testing "an uppercase HTTP url"
    (is (not (model/valid-rpc-url? "HTTP://valid.com"))))
  (testing "an http url"
    (is (model/valid-rpc-url? "http://valid.com")))
  (testing "an https url"
    (is (model/valid-rpc-url? "https://valid.something.else")))
  (testing "a fully qualified url"
    (is (model/valid-rpc-url? "https://mainnet.infura.io:6523/v3/f315575765b14720b32382a61a89341a")))
  (testing "an ip address"
    (is (model/valid-rpc-url? "https://192.168.1.1")))
  (testing "localhost"
    (is (model/valid-rpc-url? "https://localhost")))
  (testing "a fully qualified url, ip address"
    (is (model/valid-rpc-url? "https://192.168.1.1:6523/z6GCTmjdP3FETEJmMBI4")))
  (testing "an https url not on the default port"
    (is (model/valid-rpc-url? "https://valid.something.else:65323"))))

(deftest new-network-test
  (let [actual (model/new-network "random-id"
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
  (let [actual (model/new-network "random-id"
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
    (is (model/valid-manage? {:url   {:value "http://valid.com"}
                              :name  {:value "valid"}
                              :chain {:value "valid"}})))
  (testing "invalid url"
    (is (not (model/valid-manage? {:url   {:value "invalid"}
                                   :name  {:value "valid"}
                                   :chain {:value "valid"}}))))

  (testing "invalid name"
    (is (not (model/valid-manage? {:url   {:value "http://valid.com"}
                                   :name  {:value ""}
                                   :chain {:value "valid"}}))))

  (testing "invalid chain"
    (is (not (model/valid-manage? {:url   {:value "http://valid.com"}
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
           (model/set-input {:db {:networks/manage {:url   {:value "something"
                                                            :error true}
                                                    :name  {:value ""
                                                            :error false}
                                                    :chain {:value "mainnet"
                                                            :error false}}}}
                            :url "http://valid.com")))))

(deftest not-save-invalid-url
  (testing "it does not save a network with an invalid url"
    (is (nil? (model/save {:random-id-generator  (constantly "random")
                           :db {:networks/manage {:url {:value "wrong"}
                                                  :chain {:value "1"}
                                                  :name {:value "empty"}}
                                :multiaccount {}}}
                          {})))))

(deftest save-valid-network
  (testing "save a valid network"
    (is (some? (model/save {:random-id-generator  (constantly "random")
                            :db {:networks/manage {:url {:value "http://valid.com"}
                                                   :chain {:value "mainnet"}
                                                   :name {:value "valid"}}
                                 :multiaccount {}}}
                           {})))))

(deftest not-save-non-unique-id
  (testing "it does not save a network with network-id already defined"
    (let [failure (reagent/atom false)]
      (do (model/save {:random-id-generator  (constantly "random")
                       :db {:networks/manage {:url {:value "http://valid.com"}
                                              :chain {:value :mainnet}
                                              :name {:value "valid"}}
                            :multiaccount {:networks/networks {"randomid"
                                                               {:id     "randomid"
                                                                :name   "network-name"
                                                                :config {:NetworkId      1
                                                                         :DataDir        "/ethereum/mainnet_rpc"
                                                                         :UpstreamConfig {:Enabled true
                                                                                          :URL     "upstream-url"}}}}}}}
                      {:chain-id-unique? true
                       :on-failure #(reset! failure true)})
          (is @failure)))))

(deftest save-valid-network-with-unique-check
  (testing "save a valid network with network-id not already defined"
    (is (some? (model/save {:random-id-generator  (constantly "random")
                            :db {:networks/manage {:url {:value "http://valid.com"}
                                                   :chain {:value :mainnet}
                                                   :name {:value "valid"}}
                                 :multiaccount {:networks/networks {"randomid"
                                                                    {:id     "randomid"
                                                                     :name   "network-name"
                                                                     :config {:NetworkId      3
                                                                              :DataDir        "/ethereum/mainnet_rpc"
                                                                              :UpstreamConfig {:Enabled true
                                                                                               :URL     "upstream-url"}}}}}}}
                           {:chain-id-unique? true})))))

(deftest save-with-id-override
  (testing "save a valid network with id override"
    (let [result (model/save {:random-id-generator  (constantly "random")
                              :db {:networks/manage {:url {:value "http://valid.com"}
                                                     :chain {:value :mainnet}
                                                     :name {:value "valid"}}
                                   :multiaccount {}}}
                             {:network-id "override"})]
      (is (some? (get-in result [:db :multiaccount :networks/networks "override"]))))))

(deftest get-network-id-for-chain-id
  (testing "get the first network id for the given chain-id"
    (is (= "randomid" (model/get-network-id-for-chain-id {:db {:multiaccount {:networks/networks {"randomid"
                                                                                                  {:id     "randomid"
                                                                                                   :name   "network-name"
                                                                                                   :config {:NetworkId      1
                                                                                                            :DataDir        "/ethereum/mainnet_rpc"
                                                                                                            :UpstreamConfig {:Enabled true
                                                                                                                             :URL     "upstream-url"}}}}}}}
                                                         1)))))
