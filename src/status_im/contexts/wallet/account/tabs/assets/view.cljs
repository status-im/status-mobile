(ns status-im.contexts.wallet.account.tabs.assets.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.account.tabs.assets.style :as style]
    [status-im.contexts.wallet.common.token-value.view :as token-value]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [tokens-loading?       (rf/sub [:wallet/current-viewing-account-tokens-loading?])
        tokens                (rf/sub [:wallet/current-viewing-account-token-values])
        {:keys [watch-only?]} (rf/sub [:wallet/current-viewing-account])]
    (if tokens-loading?
      [quo/skeleton-list
       {:content       :assets
        :parent-height 560
        :animated?     false}]
      [rn/flat-list
       {:render-fn               token-value/view
        :style                   {:flex 1}
        :data                    tokens
        :render-data             {:watch-only? watch-only?}
        :content-container-style style/list-container-style}])))
