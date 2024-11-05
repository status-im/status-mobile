(ns status-im.contexts.wallet.home.tabs.assets.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.common.token-value.view :as token-value]
    [status-im.contexts.wallet.home.tabs.assets.style :as style]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [tokens-loading?  (rf/sub [:wallet/home-tokens-loading?])
        {:keys [tokens]} (rf/sub [:wallet/aggregated-token-values-and-balance])]
    (if tokens-loading?
      [quo/skeleton-list
       {:content       :assets
        :parent-height 560
        :animated?     false}]
      [rn/flat-list
       {:render-fn               token-value/view
        :data                    tokens
        :render-data             {:entry-point :wallet-stack}
        :content-container-style style/list-container}])))
