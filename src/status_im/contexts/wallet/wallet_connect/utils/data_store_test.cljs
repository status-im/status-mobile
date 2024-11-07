(ns status-im.contexts.wallet.wallet-connect.utils.data-store-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [clojure.string :as string]
    [status-im.contexts.wallet.common.utils.networks :as network-utils]
    [status-im.contexts.wallet.wallet-connect.utils.data-store :as sut]
    [utils.string]))

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

(deftest get-total-connected-dapps-test
  (testing "returns the total number of connected dApps plus 1"
    (let [db {:wallet-connect/sessions [{:url "https://dapp1.com"}
                                        {:url "https://dapp2.com"}]}]
      (is (= 3 (sut/get-total-connected-dapps db)))))

  (testing "returns 1 when there are no connected dApps"
    (let [db {:wallet-connect/sessions []}]
      (is (= 1 (sut/get-total-connected-dapps db)))))

  (testing "handles nil sessions correctly"
    (let [db {:wallet-connect/sessions nil}]
      (is (= 1 (sut/get-total-connected-dapps db))))))

(deftest get-session-by-topic-test
  (testing "returns the correct session based on the topic"
    (let [db    {:wallet-connect/sessions [{:topic "topic1" :url "https://dapp1.com"}
                                           {:topic "topic2" :url "https://dapp2.com"}]}
          topic "topic1"]
      (is (= {:topic "topic1" :url "https://dapp1.com"}
             (sut/get-session-by-topic db topic)))))

  (testing "returns nil if no matching session is found"
    (let [db    {:wallet-connect/sessions [{:topic "topic1" :url "https://dapp1.com"}]}
          topic "topic2"]
      (is (nil? (sut/get-session-by-topic db topic)))))

  (testing "handles nil sessions correctly"
    (let [db    {:wallet-connect/sessions nil}
          topic "topic1"]
      (is (nil? (sut/get-session-by-topic db topic)))))

  (testing "handles empty sessions correctly"
    (let [db    {:wallet-connect/sessions []}
          topic "topic1"]
      (is (nil? (sut/get-session-by-topic db topic))))));

(deftest get-account-by-session-test
  (testing "get-account-by-session function"
    (let [db      {:wallet {:accounts {"0x123" {:address "0x123"}
                                       "0x456" {:address "0x456"}
                                       "0x789" {:address "0x789"}}}}
          session {:accounts ["network:0x456"]}]
      (with-redefs [network-utils/split-network-full-address (fn [addr]
                                                               (string/split addr #":"))]
        (is (= (sut/get-account-by-session db session) {:address "0x456"}))))

    (let [db      {:wallet {:accounts {"0x123" {:address "0x123"}
                                       "0x456" {:address "0x456"}
                                       "0x789" {:address "0x789"}}}}
          session {:accounts ["network:0x999"]}]
      (with-redefs [network-utils/split-network-full-address (fn [addr]
                                                               (string/split addr #":"))]
        (is (= (sut/get-account-by-session db session) nil))))))
