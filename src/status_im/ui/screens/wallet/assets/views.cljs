(ns status-im.ui.screens.wallet.assets.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.button.view :as button]
            [status-im.i18n :as i18n]
            [status-im.ui.components.styles :as component.styles]
            [status-im.ui.components.button.styles :as button.styles]
            [status-im.ui.screens.wallet.components.views :as components]
            [status-im.ui.screens.wallet.assets.styles :as assets.styles]
            [status-im.ui.screens.wallet.main.styles :as main.styles]
            [status-im.ui.components.tabs.views :as tabs]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.screens.wallet.transactions.views :as transactions]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.chat-icon.styles :as chat-icon.styles]
            [status-im.utils.money :as money]))


(defview my-token-tab-title [active?]
  (letsubs [ {:keys [symbol]} [:token-balance]]
    [react/text {:uppercase? true
                 :style      (assets.styles/tab-title active?)}
     (i18n/label :t/wallet-my-token {:symbol symbol})]))

(defview my-token-tab-content []
  (letsubs [syncing? [:syncing?]
            {:keys [symbol
                    amount
                    decimals
                    usd-value]} [:token-balance]]
    [react/view components.styles/flex
     [react/view {:style assets.styles/total-balance-container}
      [react/view {:style assets.styles/total-balance}
       [react/text {:style assets.styles/total-balance-value} (money/to-fixed (money/token->unit (or amount (money/bignumber 0)) decimals))]
       [react/text {:style assets.styles/total-balance-currency} symbol]]
      [react/view {:style assets.styles/value-variation}
       [react/text {:style assets.styles/value-variation-title}
        (str usd-value " " "USD")]
       [components/change-display 0.05]]
      [react/view {:style (merge button.styles/buttons-container main.styles/buttons)}
       [button/button {:disabled?  syncing?
                       :on-press   #(utils/show-popup "TODO" "Not implemented yet!")
                       :style      (assets.styles/button-bar :first)
                       :text-style assets.styles/main-button-text}
        (i18n/label :t/wallet-send-token {:symbol symbol})]
       [button/button {:disabled?  true
                       :on-press   #(utils/show-popup "TODO" "Not implemented yet!")
                       :style      (assets.styles/button-bar :last)
                       :text-style assets.styles/main-button-text}
        (i18n/label :t/wallet-exchange)]]]
     [react/view
      [react/text {:style assets.styles/transactions-title} (i18n/label :t/transactions)]
      [react/view {:flex-direction :row}
        [transactions/history-list]]]]))

(defn market-value-tab-title [active?]
  [react/text {:uppercase? true
               :style      (assets.styles/tab-title active?)}
   (i18n/label :t/wallet-market-value)])

(defn market-value-tab-content []
  [react/view
   [react/text
    "Market value goes here"]])

(def tabs-list
  [{:view-id :wallet-my-token
    :content my-token-tab-title
    :screen  my-token-tab-content}
   {:view-id :wallet-market-value
    :content market-value-tab-title
    :screen  market-value-tab-content}])

(defn token-toolbar [name symbol icon]
  [react/view assets.styles/token-toolbar
   [react/image (assoc icon :style (chat-icon.styles/image-style 64))]
   [react/text {:style assets.styles/token-name-title}
    name]
   [react/text {:style assets.styles/token-symbol-title}
    symbol]])

(defview my-token-main []
  (letsubs [current-tab                [:get :view-id]
            {:keys [symbol name icon]} [:token-balance]]
    [react/view {:style component.styles/flex}
     [status-bar/status-bar]
     [toolbar/toolbar {:style assets.styles/token-toolbar-container}
      toolbar/default-nav-back
      [token-toolbar name symbol icon]]
     [tabs/swipable-tabs tabs-list current-tab true
      {:navigation-event     :navigation-replace
       :tab-style            assets.styles/tab
       :tabs-container-style assets.styles/tabs-container}]]))