(ns status-im.contexts.wallet.home.tabs.assets.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.common.temp :as temp]
    [status-im.contexts.wallet.home.tabs.assets.style :as style]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [tokens-loading? (rf/sub [:wallet/tokens-loading?])]
    (if tokens-loading?
      [quo/skeleton-list
       {:content       :assets
        :parent-height 560
        :animated?     false}]
      [rn/flat-list
       {:render-fn               quo/token-value
        :data                    temp/tokens
        :key                     :assets-list
        :content-container-style style/list-container}])))
