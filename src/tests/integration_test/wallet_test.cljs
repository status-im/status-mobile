(ns tests.integration-test.wallet-test
  (:require
    [cljs.test :refer [deftest is]]
    [day8.re-frame.test :as rf-test]
    legacy.status-im.events
    [legacy.status-im.multiaccounts.logout.core :as logout]
    legacy.status-im.subs.root
    status-im.events
    status-im.navigation.core
    status-im.subs.root
    [test-helpers.integration :as h]
    [tests.integration-test.constants :as test-constants]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn get-default-account
  [accounts]
  (first (filter :wallet accounts)))

(def send-amount "0.0001")

(deftest wallet-send-test
  (h/log-headline :wallet-send-test)
  (rf-test/run-test-async
   (h/with-app-initialized
    (h/with-recovered-account
     (rf-test/wait-for
       [:wallet/get-wallet-token]
       (let [accs            (rf/sub [:wallet/accounts])
             default-account (get-default-account accs)
             address         (:address default-account)]
         ;; navigate to account page
         (rf/dispatch [:wallet/navigate-to-account (:address default-account)])
         (rf-test/wait-for [:wallet/navigate-to-account]
           ;; set the receipient address (same address as sender to avoid token loss)
           (rf/dispatch [:wallet/select-send-address
                         {:address    address
                          :token      true
                          :receipient address
                          :stack-id   :wallet-select-address}])
           (let [filtered-tokens (rf/sub [:wallet/tokens-filtered "eth"])]

             (rf/dispatch [:wallet/send-select-token
                           {:token    (first filtered-tokens)
                            :stack-id :wallet-select-asset}])
             (rf-test/wait-for
               [:wallet/clean-suggested-routes]
               (rf/dispatch
                [:wallet/get-suggested-routes {:amount send-amount}]))
             (rf-test/wait-for
               [:wallet/suggested-routes-success]
               (let [route (rf/sub [:wallet/wallet-send-route])]
                 (is (true? (some? route)))
                 (rf/dispatch [:wallet/send-select-amount
                               {:amount   send-amount
                                :stack-id :wallet-send-input-amount}])
                 (rf/dispatch
                  [:wallet/send-transaction
                   (security/safe-unmask-data (security/hash-masked-password
                                               (security/mask-data test-constants/password)))])
                 (rf-test/wait-for [:wallet/pending-transaction-status-changed-received]
                   (let [transaction-details (rf/sub [:wallet/send-transaction-progress])]
                     (is (= :confirmed (-> transaction-details vals first :status)))
                     (h/logout)
                     (rf-test/wait-for
                       [::logout/logout-method])))))))))))))
