(ns status-im.test.accounts.events
  (:require [cljs.test :refer-macros [deftest is testing]]
            reagent.core
            [re-frame.core :as rf]
            [day8.re-frame.test :refer-macros [run-test-sync]]
            status-im.ui.screens.db
            status-im.ui.screens.subs
            [status-im.ui.screens.events :as events]
            [status-im.ui.screens.accounts.events :as account-events]
            [status-im.constants :as constants]))

(def account-id "5648abf29215d3817bec65007be83a0f11d13ad6")

(def account-from-realm
  {:last-updated        1502965625859
   :address             "c348abf29215d3817bec65007be83a0f11d13ad6"
   :email               nil
   :signed-up?          true
   :name                "Sleepy Serene Leopardseal"
   :photo-path          "photo"
   :debug?              false
   :signing-phrase      "baby atom base"
   :status              "be the hero of your own journey"
   :network             constants/default-network
   :networks            constants/default-networks
   :wnode               constants/default-wnode
   :public-key          "0x049b3a8c04f2c5bccda91c1f5e6434ae72957e93a31c0301b4563eda1d6ce419f63c503ebaee143115f96c1f04f232a7a22ca0454e9ee3d579ad1f870315b151d0"})

(def new-account
  {:address             account-id
   :signed-up?          true
   :name                "Disloyal Trusting Rainbowfish"
   :photo-path          "new-account-photo"
   :status              "the future starts today, not tomorrow"
   :network             constants/default-network
   :networks            constants/default-networks
   :wnode               constants/default-wnode
   :signing-phrase      "long loan limo"
   :public-key          "0x04f5722fba79eb36d73263417531007f43d13af76c6233573a8e3e60f667710611feba0785d751b50609bfc0b7cef35448875c5392c0a91948c95798a0ce600847"})

(defn test-fixtures []
  (rf/reg-fx ::events/init-store #())

  (rf/reg-fx ::account-events/save-account #())
  (rf/reg-fx ::account-events/broadcast-account-update #())
  (rf/reg-fx ::account-events/send-keys-update #())

  (rf/reg-cofx
    ::account-events/get-all-accounts
    (fn [coeffects _]
      (assoc coeffects :all-accounts [account-from-realm]))))

#_(deftest accounts-events
    "load-accounts
   add-account
   account-update
   account-update-keys"

    (run-test-sync

      (test-fixtures)

      (rf/dispatch [:initialize-db])
      (rf/dispatch [:set :accounts/current-account-id account-id])

      (let [accounts (rf/subscribe [:get-accounts])]

        (testing ":load-accounts event"

          ;;Assert the initial state
          (is (and (map? @accounts) (empty? @accounts)))

          (rf/dispatch [:load-accounts])

          (is (= {(:address account-from-realm) account-from-realm} @accounts)))

        (testing ":add-account event"
          (let [new-account' (assoc new-account :network constants/default-network)]

            (rf/dispatch [:add-account new-account])

            (is (= {(:address account-from-realm) account-from-realm
                    (:address new-account)        new-account'} @accounts)))))))
