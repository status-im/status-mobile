(ns status-im.subs.wallet.activities
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :wallet/all-activities
 :<- [:wallet]
 :-> :activities)

(rf/reg-sub
 :wallet/activities-for-current-viewing-account
 :<- [:wallet/all-activities]
 :<- [:wallet/current-viewing-account-address]
 (fn [[activities current-viewing-account-address]]
   (filter (fn [{:keys [sender recipient]}]
             (or (= sender current-viewing-account-address)
                 (= recipient current-viewing-account-address)))
           activities)))
