(ns status-im.ui.screens.wallet.settings.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.wallet.styles :as wallet.styles]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]))


(defn- render-token [{:keys [symbol name icon]} visible-tokens]
  [list/item
   [list/item-image icon]
   [list/item-content
    [list/item-primary name]
    [list/item-secondary symbol]]
   [list/item-checkbox {:checked?        (contains? visible-tokens (keyword symbol))
                        :on-value-change #(re-frame/dispatch [:wallet.settings/toggle-visible-token (keyword symbol) %])}]])

(defview manage-assets []
  (letsubs [network        [:network]
            visible-tokens [:wallet.settings/visible-tokens]]
    [react/view (merge components.styles/flex {:background-color :white})
     [toolbar/toolbar #_{} {:style wallet.styles/toolbar}
      [toolbar/nav-text {:style {:color :white}}
       (i18n/label :t/done)]
      [toolbar/content-title {:color :white}
       (i18n/label :t/wallet-assets)]]
     [react/view {:style components.styles/flex}
      [list/flat-list {:data      (tokens/tokens-for (ethereum/network->chain-keyword network))
                       :render-fn #(render-token % visible-tokens)}]]]))
