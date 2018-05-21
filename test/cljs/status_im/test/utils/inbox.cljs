(ns status-im.test.utils.inbox
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.inbox :as inbox]))

(deftest address->mailserver
  (testing "with password"
    (let [address "enode://some-id:the-password@206.189.56.154:30504"]
      (is (= {:address "enode://some-id@206.189.56.154:30504"
              :password "the-password"
              :user-defined true}
             (inbox/address->mailserver address)))))
  (testing "without password"
    (let [address "enode://some-id@206.189.56.154:30504"]
      (is (= {:address "enode://some-id@206.189.56.154:30504"
              :user-defined true}
             (inbox/address->mailserver address))))))

(deftest valid-enode-address
  (testing "a valid url without password"
    (let [address "enode://1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40@167.99.209.61:30504"]
      (is (inbox/valid-enode-address? address))))
  (testing "a valid url with password"
    (let [address "enode://1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40:somepasswordwith@and:@@167.99.209.61:30504"]
      (is (inbox/valid-enode-address? address)))
    (testing "invalid url"
      (is (not (inbox/valid-enode-address? "something not valid"))))))
