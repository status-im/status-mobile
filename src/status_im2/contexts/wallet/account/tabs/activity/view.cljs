(ns status-im2.contexts.wallet.account.tabs.activity.view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.resources :as quo.resources]
    [react-native.core :as rn]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.wallet.common.empty-tab.view :as empty-tab]
    [status-im2.contexts.wallet.common.temp :as temp]
    [utils.i18n :as i18n]))

(defn activity-item
  [item]
  [:<>
   [quo/divider-date (:date item)]
   [quo/wallet-activity
    (merge {:on-press #(js/alert "Item pressed")}
           item)]])

(defn view
  []
  (let [activity-list temp/activity-list]
    (if (empty? activity-list)
      [empty-tab/view
       {:title        (i18n/label :t/no-activity)
        :description  (i18n/label :t/empty-tab-description)
        :placeholder? true}]
      [rn/flat-list
       {:data      activity-list
        :render-fn activity-item}])))
