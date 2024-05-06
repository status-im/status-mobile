(ns status-im.subs.wallet.activities
  (:require
    [re-frame.core :as rf]
    [utils.datetime :as datetime]))

(rf/reg-sub
 :wallet/all-activities
 :<- [:wallet]
 :-> :activities)

(rf/reg-sub
 :wallet/activities-for-current-viewing-account
 :<- [:wallet/all-activities]
 :<- [:wallet/current-viewing-account-address]
 (fn [[activities current-viewing-account-address]]
   (let [account-activities (filter (fn [{:keys [sender recipient]}]
                                       (or (= sender current-viewing-account-address)
                                           (= recipient current-viewing-account-address)))
                                     activities)
         grouped-by-day      (group-by (fn [{:keys [timestamp]}]
                                         (datetime/timestamp->relative-short-date (* timestamp 1000)))
                                       account-activities)]
     (map (fn [[date acts]] {:title date :data acts}) grouped-by-day))))
