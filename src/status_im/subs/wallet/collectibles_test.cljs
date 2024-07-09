(ns status-im.subs.wallet.collectibles-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    [status-im.subs.root]
    [status-im.subs.wallet.collectibles]
    [test-helpers.unit :as h]
    [utils.re-frame :as rf]))

(def collectible-owner-wallet
  {:ui       {:collectible {:details {:ownership [{:address "0x1"}]}}}
   :accounts {"0x1" {:address "0x1"
                     :name    "account 1"
                     :color   "army"}}})

(h/deftest-sub :wallet/collectible-details-owner
  [sub-name]
  (testing "correct owner of the last collectible should be returned"
    (swap! rf-db/app-db assoc :wallet collectible-owner-wallet)
    (let [collectible (-> collectible-owner-wallet :ui :collectible :details)
          result      (rf/sub [sub-name collectible])]
      (is (= {:name                      "account 1"
              :color                     "army"
              :address                   "0x1"
              :network-preferences-names #{}}
             result)))))
