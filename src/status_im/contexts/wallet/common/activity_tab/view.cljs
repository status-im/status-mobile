(ns status-im.contexts.wallet.common.activity-tab.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.common.resources :as resources]
    [status-im.contexts.wallet.common.empty-tab.view :as empty-tab]
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
  (let [theme         (quo.theme/use-theme)
        activity-list []]
    (if (empty? activity-list)
      [empty-tab/view
       {:title       (i18n/label :t/no-activity)
        :description (i18n/label :t/empty-tab-description)
        :image       (resources/get-themed-image :no-activity theme)}]
      [rn/flat-list
       {:data      activity-list
        :style     {:flex 1}
        :render-fn activity-item}])))
