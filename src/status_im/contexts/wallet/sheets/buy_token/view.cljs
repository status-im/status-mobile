(ns status-im.contexts.wallet.sheets.buy-token.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.contexts.wallet.common.temp :as temp]
            [status-im.contexts.wallet.sheets.buy-token.style :as style]
            [utils.i18n :as i18n]))

(defn view
  []
  [:<>
   [quo/drawer-top {:title (i18n/label :t/buy-tokens)}]
   [rn/flat-list
    {:data      temp/buy-tokens-list
     :style     style/list-container
     :render-fn quo/settings-item}]])
