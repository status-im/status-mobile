(ns status-im.contexts.wallet.home.tabs.assets.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.home.tabs.assets.style :as style]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [tokens-loading?  (rf/sub [:wallet/tokens-loading?])
        {:keys [tokens]} (rf/sub [:wallet/aggregated-tokens-and-balance])]
    (if tokens-loading?
      [quo/skeleton-list
       {:content       :assets
        :parent-height 560
        :animated?     false}]
      [rn/flat-list
       {:render-fn               quo/token-value
        :data                    tokens
        :content-container-style style/list-container}])))
