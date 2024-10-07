(ns status-im.contexts.wallet.wallet-connect.utils.data-store-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [status-im.contexts.wallet.wallet-connect.utils.data-store :as sut]))

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

(deftest get-network-from-request-test
  (testing "returns the correct chainId from the request"
    (let [db {:wallet-connect/current-request {:event {:params {:chainId "eip155:1"}}}}]
      (is (= "eip155:1" (sut/get-network-from-request db)))))

  (testing "returns nil if chainId is not present in the request"
    (let [db {:wallet-connect/current-request {:event {:params {}}}}]
      (is (nil? (sut/get-network-from-request db))))))

(deftest get-networks-from-proposal-test
  (testing "returns session-networks as a comma-separated string when current-proposal exists"
    (let [db {:wallet-connect/current-proposal {:request          {}
                                                :session-networks ["eip155:1" "eip155:137"]}}]
      (is (= "eip155:1,eip155:137" (sut/get-networks-from-proposal db)))))

  (testing "returns required namespaces from proposal when proposal is provided"
    (let [proposal {:params {:requiredNamespaces {:eip155 {:chains ["eip155:1" "eip155:10"]}}}}]
      (is (= "eip155:1,eip155:10" (sut/get-networks-from-proposal nil proposal)))))

  (testing "returns nil if no session-networks or requiredNamespaces are found"
    (let [db {}]
      (is (= nil (sut/get-networks-from-proposal db))))))
