(ns status-im.ui.screens.wallet.main.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [status-im.components.button.view :as btn]
            [status-im.components.drawer.view :as drawer]
            [status-im.components.list.views :as list]
            [status-im.components.react :as react]
            [status-im.components.styles :as styles]
            [status-im.components.icons.vector-icons :as vi]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.utils.config :as config]
            [status-im.utils.utils :as utils]
            [status-im.ui.screens.wallet.main.styles :as wallet.styles]
            [status-im.ui.screens.wallet.views :as wallet.views]))

(defn- show-not-implemented! []
  (utils/show-popup "TODO" "Not implemented yet!"))

(defn toolbar-title []
  [react/touchable-highlight {:on-press #(rf/dispatch [:navigate-to :wallet-list])}
   [react/view {:style wallet.styles/toolbar-title-container}
    [react/text {:style wallet.styles/toolbar-title-text
                 :font  :toolbar-title}
     (i18n/label :t/main-wallet)]
    [vi/icon
     :icons/dropdown
     {:container-style wallet.styles/toolbar-title-icon
      :color           :white}]]])

(def transaction-history-action
  {:icon      :icons/transaction-history
   :icon-opts (merge {:color :white :style {:viewBox "-108 65.9 24 24"}} wallet.styles/toolbar-icon)
   :handler   #(rf/dispatch [:navigate-to-modal :wallet-transactions])})

(defn toolbar-view []
  [toolbar/toolbar2 {:style wallet.styles/toolbar}
   [toolbar/nav-button (act/hamburger-white drawer/open-drawer!)]
   [toolbar/content-wrapper
    [toolbar-title]]
   [toolbar/actions
    [(assoc (act/opts [{:text (i18n/label :t/wallet-settings) :value show-not-implemented!}]) :icon-opts {:color :white})
     transaction-history-action]]])

(defn- change-display [change]
  (let [pos-change? (pos? change)]
    [react/view {:style (if pos-change?
                          wallet.styles/today-variation-container-positive
                          wallet.styles/today-variation-container-negative)}
     [react/text {:style (if pos-change?
                           wallet.styles/today-variation-positive
                           wallet.styles/today-variation-negative)}
      (if change
        (str (when pos-change? "+") change "%")
        "-%")]]))

(defn main-section [usd-value change error-message]
  [react/view {:style wallet.styles/main-section}
   (when error-message [wallet.views/error-message-view wallet.styles/error-container wallet.styles/error-message])
   [react/view {:style wallet.styles/total-balance-container}
    [react/view {:style wallet.styles/total-balance}
     [react/text {:style wallet.styles/total-balance-value} usd-value]
     [react/text {:style wallet.styles/total-balance-currency} "USD"]]
    [react/view {:style wallet.styles/value-variation}
     [react/text {:style wallet.styles/value-variation-title}
      (i18n/label :t/wallet-total-value)]
     [change-display change]]
    [btn/buttons {:style wallet.styles/buttons :button-text-style wallet.styles/main-button-text}
     [{:text     (i18n/label :t/wallet-send)
       :on-press show-not-implemented! ;; #(rf/dispatch [:navigate-to :wallet-send-transaction])
       :disabled? (not config/wallet-wip-enabled?)}
      {:text     (i18n/label :t/wallet-request)
       :on-press show-not-implemented! ;; #(rf/dispatch [:navigate-to :wallet-request-transaction])
       :disabled? (not config/wallet-wip-enabled?)}
      {:text      (i18n/label :t/wallet-exchange)
       :disabled? true}]]]])

(defn- token->image [id]
  (case id
    "eth" {:source (:ethereum resources/assets) :style (wallet.styles/asset-border styles/color-gray-transparent-light)}))

(defn render-assets-fn [{:keys [id currency amount]}]
  ;; TODO(jeluard) Navigate to asset details screen
  #_
  [list/touchable-item show-not-implemented!
   [react/view
    [list/item
     [list/item-image {:uri :launch_logo}]
     [react/view {:style wallet.styles/asset-item-value-container}
      [react/text {:style wallet.styles/asset-item-value} (str amount)]
      [react/text {:style      wallet.styles/asset-item-currency
                   :uppercase? true}
       id]]
     [list/item-icon {:icon :icons/forward}]]]]
  [react/view
   [list/item
    (let [{:keys [source style]} (token->image id)]
      [list/item-image source style])
    [react/view {:style wallet.styles/asset-item-value-container}
     [react/text {:style wallet.styles/asset-item-value} (str amount)]
     [react/text {:style      wallet.styles/asset-item-currency
               :uppercase? true}
      id]]]])

(defn render-add-asset-fn [{:keys [id currency amount]}]
  [list/touchable-item show-not-implemented!
   [react/view
    [list/item
     [list/item-icon {:icon :icons/add :style wallet.styles/add-asset-icon :icon-opts {:color :blue}}]
     [react/view {:style wallet.styles/asset-item-value-container}
      [react/text {:style wallet.styles/add-asset-text}
       (i18n/label :t/wallet-add-asset)]]]]])

(defn asset-section [eth prices-loading? balance-loading?]
  (let [assets [{:id "eth" :currency :eth :amount eth}]]
    [react/view {:style wallet.styles/asset-section}
     [react/text {:style wallet.styles/asset-section-title} (i18n/label :t/wallet-assets)]
     [list/section-list
      {:sections                 [{:key        :assets
                                   :data       assets
                                   :renderItem (list/wrap-render-fn render-assets-fn)}
                                  {:key        :add-asset
                                   :data       [{}]
                                   :renderItem (list/wrap-render-fn render-add-asset-fn)}]
       :render-section-header-fn #()
       :on-refresh               #(rf/dispatch [:update-wallet])
       :refreshing               (boolean (or prices-loading? balance-loading?))}]]))

(defview wallet []
  (letsubs [eth-balance      [:eth-balance]
            portfolio-value  [:portfolio-value]
            portfolio-change [:portfolio-change]
            prices-loading?  [:prices-loading?]
            balance-loading? [:wallet/balance-loading?]
            error-message    [:wallet/error-message?]]
    [react/view {:style wallet.styles/wallet-container}
     [toolbar-view]
     [react/view {:style styles/flex}
      [main-section portfolio-value portfolio-change error-message]
      [asset-section eth-balance prices-loading? balance-loading?]]]))
