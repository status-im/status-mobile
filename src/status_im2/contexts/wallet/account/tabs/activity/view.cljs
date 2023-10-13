(ns status-im2.contexts.wallet.account.tabs.activity.view
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [status-im2.contexts.wallet.common.empty-tab.view :as empty-tab]
    [utils.i18n :as i18n]))


(defn view
  []
  (let [activity-list [1]]
  (if (empty? activity-list)
    [empty-tab/view
     {:title        (i18n/label :t/no-activity)
      :description  (i18n/label :t/empty-tab-description)
      :placeholder? true}]
    [rn/view]
    )))
