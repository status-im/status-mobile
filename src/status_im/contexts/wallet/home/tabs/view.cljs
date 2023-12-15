(ns status-im.contexts.wallet.home.tabs.view
  (:require
    [react-native.core :as rn]
    [status-im.contexts.wallet.common.activity-tab.view :as activity]
    [status-im.contexts.wallet.common.collectibles-tab.view :as collectibles]
    [status-im.contexts.wallet.home.tabs.assets.view :as assets]
    [status-im.contexts.wallet.home.tabs.style :as style]))

(defn view
  [{:keys [selected-tab]}]
  [rn/view {:style style/container}
   (case selected-tab
     :assets       [assets/view]
     :collectibles [collectibles/view]
     [activity/view])])
