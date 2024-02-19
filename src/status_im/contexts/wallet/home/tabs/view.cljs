(ns status-im.contexts.wallet.home.tabs.view
  (:require
    [react-native.core :as rn]
    [status-im.contexts.wallet.common.activity-tab.view :as activity]
    [status-im.contexts.wallet.common.collectibles-tab.view :as collectibles]
    [status-im.contexts.wallet.home.tabs.assets.view :as assets]
    [status-im.contexts.wallet.home.tabs.style :as style]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [selected-tab]}]
  (let [collectible-list (rf/sub [:wallet/all-collectibles-list])]
    [rn/view {:style style/container}
     (case selected-tab
       :assets       [assets/view]
       :collectibles [collectibles/view
                      {:collectibles         collectible-list
                       :on-end-reached       #(rf/dispatch [:wallet/request-collectibles-for-all-accounts {}])
                       :on-collectible-press (fn [{:keys [id]}]
                                               (rf/dispatch [:wallet/get-collectible-details id]))}]
       [activity/view])]))
