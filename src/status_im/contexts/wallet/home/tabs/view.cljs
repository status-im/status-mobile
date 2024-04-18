(ns status-im.contexts.wallet.home.tabs.view
  (:require
    [react-native.core :as rn]
    [status-im.contexts.wallet.collectible.options.view :as options-drawer]
    [status-im.contexts.wallet.common.activity-tab.view :as activity]
    [status-im.contexts.wallet.common.collectibles-tab.view :as collectibles]
    [status-im.contexts.wallet.home.tabs.assets.view :as assets]
    [status-im.contexts.wallet.home.tabs.style :as style]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [selected-tab]}]
  (let [collectible-list     (rf/sub [:wallet/all-collectibles-list-in-selected-networks])
        request-collectibles #(rf/dispatch
                               [:wallet/request-collectibles-for-all-accounts {}])]
    [rn/view {:style style/container}
     (case selected-tab
       :assets       [assets/view]
       :collectibles [collectibles/view
                      {:collectibles              collectible-list
                       :on-collectible-long-press (fn [{:keys [preview-url collectible-details id]}]
                                                    (let [chain-id (get-in id [:contract-id :chain-id])
                                                          address  (get-in id [:contract-id :address])]
                                                      (rf/dispatch
                                                       [:show-bottom-sheet
                                                        {:content (fn []
                                                                    [options-drawer/view
                                                                     {:chain-id chain-id
                                                                      :address  address
                                                                      :name     (:name
                                                                                 collectible-details)
                                                                      :image    (:uri
                                                                                 preview-url)}])}])))
                       :on-end-reached            request-collectibles
                       :on-collectible-press      (fn [{:keys [id]}]
                                                    (rf/dispatch [:wallet/get-collectible-details id]))}]
       [activity/view {:activities []}])]))
