(ns status-im.contexts.wallet.account.tabs.assets.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.common.token-value.view :as token-value]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [tokens-loading? (rf/sub [:wallet/tokens-loading?])
        tokens          (rf/sub [:wallet/account-token-values])]
    (if tokens-loading?
      [quo/skeleton-list
       {:content       :assets
        :parent-height 560
        :animated?     false}]
      [rn/flat-list
       {:render-fn               token-value/view
        :style                   {:flex 1}
        :data                    tokens
        :content-container-style {:padding-horizontal 8}}])))
