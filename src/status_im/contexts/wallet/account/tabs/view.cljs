(ns status-im.contexts.wallet.account.tabs.view
  (:require
    [react-native.core :as rn]
    [status-im.contexts.wallet.account.tabs.about.view :as about]
    [status-im.contexts.wallet.account.tabs.assets.view :as assets]
    [status-im.contexts.wallet.collectible.options.view :as options-drawer]
    [status-im.contexts.wallet.common.activity-tab.view :as activity]
    [status-im.contexts.wallet.common.collectibles-tab.view :as collectibles]
    [status-im.contexts.wallet.common.empty-tab.view :as empty-tab]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def on-collectible-press #(rf/dispatch [:wallet/navigate-to-collectible-details %]))

(defn- on-collectible-long-press
  [{:keys [preview-url collectible-details id]}]
  (rf/dispatch [:show-bottom-sheet
                {:content (fn []
                            [options-drawer/view
                             {:name  (:name collectible-details)
                              :image (:uri preview-url)
                              :id    id}])}]))

(defn- on-end-reached
  []
  (rf/dispatch [:wallet/request-collectibles-for-current-viewing-account]))

(defn- collectibles-tab
  []
  (let [updating?               (rf/sub [:wallet/current-viewing-account-collectibles-updating?])
        collectible-list        (rf/sub
                                 [:wallet/current-viewing-account-collectibles-in-selected-networks])
        current-account-address (rf/sub [:wallet/current-viewing-account-address])]
    [collectibles/view
     {:loading?                  updating?
      :collectibles              collectible-list
      :current-account-address   current-account-address
      :on-end-reached            on-end-reached
      :on-collectible-press      on-collectible-press
      :on-collectible-long-press on-collectible-long-press}]))

(defn view
  [{:keys [selected-tab]}]
  [rn/view {:style {:flex 1}}
   (case selected-tab
     :assets       [assets/view]
     :collectibles [collectibles-tab]
     :activity     [activity/view]
     :permissions  [empty-tab/view
                    {:title        (i18n/label :t/no-permissions)
                     :description  (i18n/label :t/no-collectibles-description)
                     :placeholder? true}]
     [about/view])])
