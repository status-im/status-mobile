(ns status-im.contexts.wallet.account.tabs.view
  (:require
    [react-native.core :as rn]
    [status-im.contexts.wallet.account.tabs.about.view :as about]
    [status-im.contexts.wallet.account.tabs.assets.view :as assets]
    [status-im.contexts.wallet.account.tabs.dapps.view :as dapps]
    [status-im.contexts.wallet.common.activity-tab.view :as activity]
    [status-im.contexts.wallet.common.collectibles-tab.view :as collectibles]
    [status-im.contexts.wallet.common.empty-tab.view :as empty-tab]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [selected-tab]}]
  (let [current-viewing-account (rf/sub [:wallet/current-viewing-account-address])]
    [rn/view {:style {:flex 1}}
     (case selected-tab
       :assets       [assets/view]
       :collectibles [collectibles/view {:address current-viewing-account}]
       :activity     [activity/view]
       :permissions  [empty-tab/view
                      {:title        (i18n/label :t/no-permissions)
                       :description  (i18n/label :t/no-collectibles-description)
                       :placeholder? true}]
       :dapps        [dapps/view]
       [about/view])]))
