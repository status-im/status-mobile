(ns status-im2.contexts.wallet.account.tabs.view
  (:require
    [react-native.core :as rn]
    [status-im2.contexts.wallet.account.tabs.about.view :as about]
    [status-im2.contexts.wallet.account.tabs.dapps.view :as dapps]
    [status-im2.contexts.wallet.common.activity-tab.view :as activity]
    [status-im2.contexts.wallet.common.collectibles-tab.view :as collectibles]
    [status-im2.contexts.wallet.common.empty-tab.view :as empty-tab]
    [status-im2.contexts.wallet.common.token-value.view :as token-value]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [selected-tab]}]
  (let [tokens (rf/sub [:wallet/account-token-values])]
    (case selected-tab
      :assets       [rn/flat-list
                     {:render-fn               token-value/view
                      :data                    tokens
                      :content-container-style {:padding-horizontal 8}}]
      :collectibles [collectibles/view]
      :activity     [activity/view]
      :permissions  [empty-tab/view
                     {:title        (i18n/label :t/no-permissions)
                      :description  (i18n/label :t/no-collectibles-description)
                      :placeholder? true}]
      :dapps        [dapps/view]
      [about/view])))
