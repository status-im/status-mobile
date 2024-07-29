(ns utils.network.core-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [utils.network.core :as network.core]))

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
    (is (network.core/valid-rpc-url? "https://mainnet.infura.io:6523/v3/some-token")))
  (testing "an ip address"
    (is (network.core/valid-rpc-url? "https://192.168.1.1")))
  (testing "localhost"
    (is (network.core/valid-rpc-url? "https://localhost")))
  (testing "a fully qualified url, ip address"
    (is (network.core/valid-rpc-url? "https://192.168.1.1:6523/z6GCTmjdP3FETEJmMBI4")))
  (testing "an https url not on the default port"
    (is (network.core/valid-rpc-url? "https://valid.something.else:65323"))))

(deftest valid-manage-test
  (testing "a valid manage"
    (is (network.core/valid-manage? {:url    {:value "http://valid.com"}
                                     :name   {:value "valid"}
                                     :symbol {:value "valid"}
                                     :chain  {:value "valid"}})))
  (testing "invalid url"
    (is (not (network.core/valid-manage? {:url    {:value "invalid"}
                                          :name   {:value "valid"}
                                          :symbol {:value "valid"}
                                          :chain  {:value "valid"}}))))

  (testing "invalid name"
    (is (not (network.core/valid-manage? {:url    {:value "http://valid.com"}
                                          :name   {:value ""}
                                          :symbol {:value "valid"}
                                          :chain  {:value "valid"}}))))

  (testing "invalid chain"
    (is (not (network.core/valid-manage? {:url    {:value "http://valid.com"}
                                          :name   {:value "valid"}
                                          :symbol {:value "valid"}
                                          :chain  {:value ""}}))))

  (testing "invalid symbol"
    (is (not (network.core/valid-manage? {:url    {:value "http://valid.com"}
                                          :name   {:value "valid"}
                                          :symbol {:value ""}
                                          :chain  {:value "valid"}})))))
