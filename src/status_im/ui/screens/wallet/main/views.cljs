(ns status-im.ui.screens.wallet.main.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [status-im.components.button.view :as btn]
            [status-im.components.drawer.view :as drawer]
            [status-im.components.list.views :as list]
            [status-im.components.react :as react]
            [status-im.components.icons.vector-icons :as vi]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.utils.config :as config]
            [status-im.utils.utils :as utils]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.wallet.main.styles :as styles]
            [status-im.ui.screens.wallet.styles :as wallet.styles]
            [status-im.components.styles :as components.styles]
            [status-im.ui.screens.wallet.views :as wallet.views]))

(defn- show-not-implemented! []
  (utils/show-popup "TODO" "Not implemented yet!"))

(defn toolbar-title []
  [react/touchable-highlight {:on-press #(rf/dispatch [:navigate-to :wallet-list])}
   [react/view {:style styles/toolbar-title-container}
    [react/text {:style styles/toolbar-title-text
                 :font  :toolbar-title}
     (i18n/label :t/main-wallet)]
    [vi/icon
     :icons/dropdown
     {:container-style styles/toolbar-title-icon
      :color           :white}]]])

(def transaction-history-action
  {:icon      :icons/transaction-history
   :icon-opts (merge {:color :white :style {:viewBox "-108 65.9 24 24"}} styles/toolbar-icon)
   :handler   #(rf/dispatch [:navigate-to :wallet-transactions])})

(defn toolbar-view []
  [toolbar/toolbar2 {:style wallet.styles/toolbar}
   [toolbar/nav-button (act/hamburger-white drawer/open-drawer!)]
   [toolbar/content-wrapper
    [toolbar-title]]
   [toolbar/actions
    [(assoc (act/opts [{:text (i18n/label :t/wallet-settings) :value show-not-implemented!}]) :icon-opts {:color :white})
     transaction-history-action]]])

(defn- change-display [change]
  (let [pos-change? (or (pos? change) (zero? change))]
    [react/view {:style (if pos-change?
                          styles/today-variation-container-positive
                          styles/today-variation-container-negative)}
     [react/text {:style (if pos-change?
                           styles/today-variation-positive
                           styles/today-variation-negative)}
      (if change
        (str (when pos-change? "+") change "%")
        "-%")]]))

(defn main-section [usd-value change error-message]
  [react/view {:style styles/main-section}
   (when error-message
     [wallet.views/error-message-view styles/error-container styles/error-message])
   [react/view {:style styles/total-balance-container}
    [react/view {:style styles/total-balance}
     [react/text {:style styles/total-balance-value} usd-value]
     [react/text {:style styles/total-balance-currency} "USD"]]
    [react/view {:style styles/value-variation}
     [react/text {:style styles/value-variation-title}
      (i18n/label :t/wallet-total-value)]
     [change-display change]]
    [btn/buttons {:style styles/buttons :button-text-style styles/main-button-text}
     [{:text      (i18n/label :t/wallet-send)
       :on-press  #(do (rf/dispatch [:navigate-to :wallet-send-transaction])
                       (when platform/android?
                         (rf/dispatch [:request-permissions [:camera]])))
       :disabled? (not config/wallet-wip-enabled?)}
      {:text     (i18n/label :t/wallet-request)
       :on-press #(rf/dispatch [:navigate-to :wallet-request-transaction])
       :disabled? (not config/wallet-wip-enabled?)}
      {:text      (i18n/label :t/wallet-exchange)
       :disabled? true}]]]])

(defn- token->image [id]
  (case id
    "eth" {:source (:ethereum resources/assets) :style (styles/asset-border components.styles/color-gray-transparent-light)}))

(defn add-asset []
  [list/touchable-item show-not-implemented!
   [react/view
    [list/item
     [list/item-icon {:icon :icons/add :style styles/add-asset-icon :icon-opts {:color :blue}}]
     [react/view {:style styles/asset-item-value-container}
      [react/text {:style styles/add-asset-text}
       (i18n/label :t/wallet-add-asset)]]]]])

(defn render-asset [{:keys [id currency amount]}]
  ;; TODO(jeluard) Navigate to asset details screen
  #_
  [list/touchable-item show-not-implemented!
   [react/view
    [list/item
     [list/item-image {:uri :launch_logo}]
     [react/view {:style styles/asset-item-value-container}
      [react/text {:style styles/asset-item-value} (str amount)]
      [react/text {:style      styles/asset-item-currency
                   :uppercase? true}
       id]]
     [list/item-icon {:icon :icons/forward}]]]]
  (if id
    [react/view
     [list/item
      (let [{:keys [source style]} (token->image id)]
        [list/item-image source style])
      [react/view {:style styles/asset-item-value-container}
       [react/text {:style styles/asset-item-value} (str amount)]
       [react/text {:style      styles/asset-item-currency
                    :uppercase? true}
        id]]]]
    [add-asset]))

(defn asset-section [eth prices-loading? balance-loading?]
  (let [assets [{:id "eth" :currency :eth :amount eth}]]
    [react/view {:style styles/asset-section}
     [react/text {:style styles/asset-section-title} (i18n/label :t/wallet-assets)]
     [list/flat-list
      {:data       (conj assets {}) ;; Extra map triggers rendering for add-asset
       :render-fn  render-asset
       :on-refresh #(rf/dispatch [:update-wallet])
       :refreshing (boolean (or prices-loading? balance-loading?))}]]))

(defview wallet []
  (letsubs [eth-balance      [:eth-balance]
            portfolio-value  [:portfolio-value]
            portfolio-change [:portfolio-change]
            prices-loading?  [:prices-loading?]
            balance-loading? [:wallet/balance-loading?]
            error-message    [:wallet/error-message?]]
    [react/view {:style wallet.styles/wallet-container}
     [toolbar-view]
     [react/view components.styles/flex
      [main-section portfolio-value portfolio-change error-message]
      [asset-section eth-balance prices-loading? balance-loading?]]]))
