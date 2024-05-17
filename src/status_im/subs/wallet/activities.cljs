(ns status-im.subs.wallet.activities
  (:require
    [re-frame.core :as rf]
    [status-im.contexts.wallet.common.activity-tab.constants :as constants]
    [utils.datetime :as datetime]))

(rf/reg-sub
 :wallet/all-activities
 :<- [:wallet]
 :-> :activities)

(rf/reg-sub :wallet/activities-for-current-viewing-account
 :<- [:wallet/all-activities]
 :<- [:wallet/current-viewing-account-address]
 (fn [[activities current-viewing-account-address]]
   (->> activities
        (filter (fn [{:keys [sender recipient activity-type]}]
                  (let [receiving-activity? (= activity-type constants/wallet-activity-type-receive)
                        relevant-address    (if receiving-activity? recipient sender)]
                    (= relevant-address current-viewing-account-address))))
        (distinct)
        (group-by (fn [{:keys [timestamp]}]
                    (datetime/timestamp->relative-short-date (* timestamp 1000))))
        (map (fn [[date activities]]
               {:title date :data activities :timestamp (:timestamp (first activities))}))
        (sort-by (fn [{:keys [timestamp]}] (- timestamp))))))
