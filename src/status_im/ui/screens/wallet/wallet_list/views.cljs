(ns status-im.ui.screens.wallet.wallet-list.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.wallet.wallet-list.styles :as styles]
            [status-im.utils.utils :as utils]))

(defn- toolbar-view [transactions]
  [toolbar/toolbar {:style styles/toolbar}
   [toolbar/nav-clear-text (i18n/label :t/done) #(re-frame/dispatch [:navigate-back])]
   [toolbar/content-title (i18n/label :t/wallets)]
   [toolbar/actions
    [(actions/add-wallet #(utils/show-popup "TODO" "Not implemented!"))]]])

(defn- select-wallet []
  (utils/show-popup "TODO" "Not implemented!"))

(defn- wallet-list-item [{:keys [name amount assets active?]}]
  (let [asset-list (string/join "  " (concat [(i18n/label :t/eth)] assets))]
    [react/touchable-highlight {:on-press select-wallet}
     [react/view {:style (merge styles/wallet-item (if active? styles/active-wallet-item))}
      [react/view {:style styles/wallet-info}
       [react/text {:style styles/wallet-name} name]
       [react/view {:style styles/total-balance}
        [react/text {:style styles/total-balance-value} amount]
        [react/text {:style styles/total-balance-currency} (i18n/label :t/usd-currency)]]
       [react/text {:style styles/asset-list} asset-list]]
      [react/icon :forward_gray styles/select-wallet-icon]]]))

(defview wallet-list []
  (letsubs [wallets [:wallet.list/all]]
    [react/scroll-view {:style styles/wallet-list-screen}
     [react/text {:style styles/wallet-list-title}
      (i18n/label :t/your-wallets)]
     [list/flat-list {:data          wallets
                      :render-fn     wallet-list-item
                      :style         styles/wallet-list
                      :scrollEnabled false}]]))

(defview wallet-list-screen []
  []
  [react/view {:style styles/screen-container}
   [status-bar/status-bar]
   [toolbar-view]
   [wallet-list]])
