(ns status-im.contexts.wallet.wallet-connect.core-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [status-im.contexts.wallet.wallet-connect.core :as sut]))

(deftest get-current-request-dapp-test
  (testing "returns the correct dapp based on the request's origin"
    (let [request  {:event {:verifyContext {:verified {:origin "https://dapp.com"}}}}
          sessions [{:url "https://dapp.com"}
                    {:url "https://anotherdapp.com"}]]
      (is (= {:url "https://dapp.com"}
             (sut/get-current-request-dapp request sessions)))))

  (testing "returns nil if no matching dapp is found"
    (let [request  {:event {:verifyContext {:verified {:origin "https://dapp.com"}}}}
          sessions [{:url "https://anotherdapp.com"}]]
      (is (nil? (sut/get-current-request-dapp request sessions))))))

(deftest get-dapp-redirect-url-test
  (testing "returns the native redirect URL if it exists"
    (let [session {:peer {:metadata {:redirect {:native "native://redirect-url"}}}}]
      (is (= "native://redirect-url"
             (sut/get-dapp-redirect-url session)))))

  (testing "returns nil if no redirect URL is found"
    (let [session {:peer {:metadata {}}}]
      (is (nil? (sut/get-dapp-redirect-url session))))))
