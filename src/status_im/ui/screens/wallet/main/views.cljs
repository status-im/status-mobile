(ns status-im.ui.screens.wallet.main.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.button.view :as btn]
            [status-im.ui.components.drawer.view :as drawer]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vi]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as act]
            [status-im.i18n :as i18n]
            [status-im.utils.config :as config]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.money :as money]
            [status-im.utils.utils :as utils]
            [status-im.ui.screens.wallet.main.styles :as styles]
            [status-im.ui.screens.wallet.styles :as wallet.styles]
            [status-im.ui.screens.wallet.utils :as wallet.utils]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.screens.wallet.components.views :as components]
            [status-im.ui.components.button.styles :as button.styles]
            [status-im.ui.screens.wallet.views :as wallet.views]))

(defn toolbar-title []
  [react/view {:style styles/toolbar-title-container}
   [react/text {:style           styles/toolbar-title-text
                :font            :toolbar-title
                :number-of-lines 1}
    (i18n/label :t/main-wallet)]])

(def transaction-history-action
  {:icon      :icons/transaction-history
   :icon-opts (merge {:color :white :style {:viewBox "-108 65.9 24 24"}} styles/toolbar-icon)
   :handler   #(re-frame/dispatch [:navigate-to :transactions-history])})

(defn toolbar-view []
  [toolbar/toolbar {:style wallet.styles/toolbar}
   [toolbar/nav-button (act/hamburger-white drawer/open-drawer!)]
   [toolbar/content-wrapper
    [toolbar-title]]
   [toolbar/actions
    [(assoc (act/opts [{:text  (i18n/label :t/wallet-manage-assets)
                        :value #(re-frame/dispatch [:navigate-to-modal :wallet-settings-assets])}])
       :icon-opts {:color :white})
     transaction-history-action]]])

(defn main-section [usd-value change syncing? error-message]
  [react/view {:style styles/main-section}
   (if syncing?
     wallet.views/wallet-syncing
     (when error-message
       wallet.views/error-message-view))
   [react/view {:style styles/total-balance-container}
    [react/view {:style styles/total-balance}
     [react/text {:style styles/total-balance-value} usd-value]
     [react/text {:style styles/total-balance-currency} (i18n/label :t/usd-currency)]]
    [react/view {:style (merge button.styles/buttons-container styles/buttons)}
     [btn/button {:disabled? syncing?
                  :on-press #(re-frame/dispatch [:navigate-to :wallet-send-transaction])
                  :style    (button.styles/button-bar :first) :text-style styles/main-button-text}
      (i18n/label :t/wallet-send)]
     [btn/button {:disabled? syncing?
                  :on-press #(re-frame/dispatch [:navigate-to :wallet-request-transaction])
                  :style (button.styles/button-bar :other) :text-style styles/main-button-text}
      (i18n/label :t/wallet-request)]
     [btn/button {:disabled? true :style (button.styles/button-bar :last) :text-style styles/main-button-text}
      (i18n/label :t/wallet-exchange)]]]])

(defn- render-asset [{:keys [name symbol icon decimals amount] :as asset}]
  [react/view
   [list/item
    [list/item-image icon]
    [react/view {:style styles/asset-item-value-container}
     [react/text {:style           styles/asset-item-value
                  :number-of-lines 1
                  :ellipsize-mode  :tail}
      (wallet.utils/format-amount amount decimals)]
     [react/text {:style           styles/asset-item-currency
                  :uppercase?      true
                  :number-of-lines 1}
      symbol]]]])

(defn asset-section [network balance visible-tokens prices-loading? balance-loading?]
  (let [tokens (filter #(contains? visible-tokens (:symbol %)) (tokens/tokens-for (ethereum/network->chain-keyword network)))
        assets (map #(assoc % :amount (get balance (:symbol %))) (concat [tokens/ethereum] (when config/erc20-enabled? tokens)))]
    [react/view {:style styles/asset-section}
     [react/text {:style styles/asset-section-title} (i18n/label :t/wallet-assets)]
     [list/flat-list
      {:data       assets
       :render-fn  render-asset
       :on-refresh #(re-frame/dispatch [:update-wallet (when config/erc20-enabled? (map :symbol tokens))])
       :refreshing (boolean (or prices-loading? balance-loading?))}]]))

(defview wallet []
  (letsubs [network          [:network]
            balance          [:balance]
            visible-tokens   [:wallet.settings/visible-tokens]
            portfolio-value  [:portfolio-value]
            portfolio-change [:portfolio-change]
            prices-loading?  [:prices-loading?]
            syncing?         [:syncing?]
            balance-loading? [:wallet/balance-loading?]
            error-message    [:wallet/error-message?]]
    [react/view {:style wallet.styles/wallet-container}
     [toolbar-view]
     [react/view components.styles/flex
      [main-section portfolio-value portfolio-change syncing? error-message]
      [asset-section network balance visible-tokens prices-loading? balance-loading?]]]))
