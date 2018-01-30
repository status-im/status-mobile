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
   :updates-private-key "3849320857de8efe1e1ec57e08e92ed2bce196cb8763756ae4e6e7e011c1d857de0a115b3dc7eff066afe75a8794ea9905b"
   :updates-public-key  "384975d68aec6426faacf8b4ba2c55d5a84b70a8a26eb616e06e9c9e63f95dfdf1c1c165773e1cdca2d198a0bc5386d8a6f2079414e073b4730c8f4745292a6cdfb3fa28143ad5937128643c6addf356b66962376dc8b12274d9abfb2e1c6447ac3"
   :photo-path          "photo"
   :debug?              false
   :signing-phrase      "baby atom base"
   :status              "be the hero of your own journey"
   :network             constants/default-network
   :networks            constants/default-networks
   :public-key          "0x049b3a8c04f2c5bccda91c1f5e6434ae72957e93a31c0301b4563eda1d6ce419f63c503ebaee143115f96c1f04f232a7a22ca0454e9ee3d579ad1f870315b151d0"})

(def new-account
  {:address             account-id
   :signed-up?          true
   :name                "Disloyal Trusting Rainbowfish"
   :updates-private-key "3849071831f581f5e2a4f095a53e0a697144b32ea6de9e92cc08936f2efa40d2f1702bdb131356df0930a3a0d301221f2b5"
   :updates-public-key  "38453ecc298b8b35de00c85d3217f00aa7040a7d3053dbbf6831d03c750df40b27977906692b3b5d6fec8134706b2bf65900c61130047488520cb60080a59b118cb281f3aaf65ba704c7efde8f9357d2b22fe8110b38a4dd714c1c9e108a8b067fe"
   :photo-path          "new-account-photo"
   :status              "the future starts today, not tomorrow"
   :network             constants/default-network
   :networks            constants/default-networks
   :signing-phrase      "long loan limo"
   :public-key          "0x04f5722fba79eb36d73263417531007f43d13af76c6233573a8e3e60f667710611feba0785d751b50609bfc0b7cef35448875c5392c0a91948c95798a0ce600847"})

(defn test-fixtures []
  (rf/reg-fx ::events/init-store #())

  (rf/reg-fx ::account-events/save-account #())
  (rf/reg-fx ::account-events/broadcast-account-update #())
  (rf/reg-fx ::account-events/send-keys-update #())

  (rf/reg-cofx
    :get-new-keypair!
    (fn [coeffects _]
      (assoc coeffects :keypair {:public  "new public"
                                 :private "new private"})))

  (rf/reg-cofx
    ::account-events/get-all-accounts
    (fn [coeffects _]
      (assoc coeffects :all-accounts [account-from-realm]))))

(deftest accounts-events
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
                  (:address new-account)        new-account'} @accounts))

          (testing ":account-update-keys event"

            (rf/dispatch [:account-update-keys])

            (is (= {(:address account-from-realm) account-from-realm
                    (:address new-account)        (assoc new-account'
                                                    :updates-private-key "new private"
                                                    :updates-public-key "new public")}
                   (update @accounts (:address new-account) dissoc :last-updated)))))))))
