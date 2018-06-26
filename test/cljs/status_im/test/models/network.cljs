(ns status-im.test.models.network
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.models.network :as model]))

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
  (testing "an http url"
    (is (model/valid-rpc-url? "http://valid.com")))
  (testing "an https url"
    (is (model/valid-rpc-url? "https://valid.something.else")))
  (testing "a fully qualified url"
    (is (model/valid-rpc-url? "https://mainnet.infura.io:6523/z6GCTmjdP3FETEJmMBI4")))
  (testing "an ip address"
    (is (model/valid-rpc-url? "https://192.168.1.1")))
  (testing "localhost"
    (is (model/valid-rpc-url? "https://localhost")))
  (testing "a fully qualified url, ip address"
    (is (model/valid-rpc-url? "https://192.168.1.1:6523/z6GCTmjdP3FETEJmMBI4")))
  (testing "an https url not on the default port"
    (is (model/valid-rpc-url? "https://valid.something.else:65323"))))

(deftest new-network-test
  (let [actual (model/new-network {:random-id "random-id"}
                                  "network-name"
                                  "upstream-url"
                                  :mainnet)]
    (is (= {:id     "randomid"
            :name   "network-name"
            :config {:NetworkId      1
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
    (is (= {:db {:networks/manage {:url   {:value "http://valid.com"
                                           :error false}
                                   :name  {:value ""
                                           :error true}
                                   :chain {:value "mainnet"
                                           :error false}}}}
           (model/set-input :url "http://valid.com"
                            {:db {:networks/manage {:url   {:value "something"
                                                            :error true}
                                                    :name  {:value ""
                                                            :error false}
                                                    :chain {:value "mainnet"
                                                            :error false}}}})))))

(deftest save
  (testing "it does not save a network with an invalid url"
    (is (nil? (model/save {:random-id "random"
                           :db {:networks/manage {:url {:value "wrong"}
                                                  :chain {:value "1"}
                                                  :name {:value "empty"}}
                                :account/account {}}})))))
