(ns status-im.ui.screens.wallet.assets.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
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
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.styles :as components.styles]))


(defview my-token-tab-title [active?]
  (letsubs [ {:keys [token-symbol]} [:token-balance]]
    [react/text {:uppercase? true
                 :style      (assets.styles/tab-title active?)}
     (i18n/label :t/wallet-my-token {:symbol token-symbol})]))

(defview my-token-tab-content []
  (letsubs [syncing? [:syncing?]
            {:keys [token-symbol
                    token-value
                    usd-value]} [:token-balance]]
    [react/view components.styles/flex
     [react/view {:style assets.styles/total-balance-container}
      [react/view {:style assets.styles/total-balance}
       [react/text {:style assets.styles/total-balance-value} token-value]
       [react/text {:style assets.styles/total-balance-currency} token-symbol]]
      [react/view {:style assets.styles/value-variation}
       [react/text {:style assets.styles/value-variation-title}
        (str usd-value " " "USD")]
       [components/change-display 0.05]]
      [react/view {:style (merge button.styles/buttons-container main.styles/buttons)}
       [button/button {:disabled? syncing?
                       :on-press  #()
                       :style     (button.styles/button-bar :first) :text-style assets.styles/main-button-text}
        (i18n/label :t/wallet-send-token {:symbol token-symbol})]
       [button/button {:disabled? true
                       :on-press  #()
                       :style     (button.styles/button-bar :last) :text-style assets.styles/main-button-text}
        (i18n/label :t/wallet-exchange)]]]
     [react/view
      [react/text (i18n/label :t/transactions)]
      [react/text "Transaction list goes here"]]]))

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

(defview my-token-main []
  (letsubs [current-tab [:get :view-id]
            {:keys [token-symbol token-name]} [:token-balance]]
    [react/view {:style component.styles/flex}
     [status-bar/status-bar]
     [toolbar/toolbar {}
      toolbar/default-nav-back
      [toolbar/content-title
       (str token-symbol " - " token-name)]]
     [tabs/swipable-tabs tabs-list current-tab true
      {:navigation-event     :navigation-replace
       :tab-style            assets.styles/tab
       :tabs-container-style assets.styles/tabs-container}]]))