(ns status-im.contexts.wallet.account.tabs.view
  (:require
    [react-native.core :as rn]
    [status-im.contexts.wallet.account.tabs.about.view :as about]
    [status-im.contexts.wallet.account.tabs.assets.view :as assets]
    [status-im.contexts.wallet.account.tabs.dapps.view :as dapps]
    [status-im.contexts.wallet.collectible.options.view :as options-drawer]
    [status-im.contexts.wallet.common.activity-tab.view :as activity]
    [status-im.contexts.wallet.common.collectibles-tab.view :as collectibles]
    [status-im.contexts.wallet.common.empty-tab.view :as empty-tab]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- on-collectible-press
  [{:keys [id]}]
  (rf/dispatch [:wallet/get-collectible-details id]))

(defn- on-collectible-long-press
  [{:keys [preview-url collectible-details]}]
  (rf/dispatch [:show-bottom-sheet
                {:content (fn []
                            [options-drawer/view
                             {:name  (:name collectible-details)
                              :image (:uri preview-url)}])}]))

(defn- on-end-reached
  []
  (rf/dispatch [:wallet/request-collectibles-for-current-viewing-account]))

(defn view
  [{:keys [selected-tab]}]
  (let [collectible-list        (rf/sub
                                 [:wallet/current-viewing-account-collectibles-in-selected-networks])
        current-account-address (rf/sub [:wallet/current-viewing-account-address])]
    [rn/view {:style {:flex 1}}
     (case selected-tab
       :assets       [assets/view]
       :collectibles [collectibles/view
                      {:collectibles              collectible-list
                       :current-account-address   current-account-address
                       :on-end-reached            on-end-reached
                       :on-collectible-press      on-collectible-press
                       :on-collectible-long-press on-collectible-long-press}]
       :activity     [activity/view]
       :permissions  [empty-tab/view
                      {:title        (i18n/label :t/no-permissions)
                       :description  (i18n/label :t/no-collectibles-description)
                       :placeholder? true}]
       :dapps        [dapps/view]
       [about/view])]))
