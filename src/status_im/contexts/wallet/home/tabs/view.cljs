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
  (let [collectible-list (rf/sub [:wallet/all-collectibles])]
    [rn/view {:style style/container}
     (case selected-tab
       :assets       [assets/view]
       :collectibles [collectibles/view
                      {:collectibles         collectible-list
                       :on-collectible-press (fn [id]
                                               (rf/dispatch [:wallet/get-collectible-details id])
                                               (rf/dispatch [:navigate-to :wallet-collectible]))}]
       [activity/view])]))
