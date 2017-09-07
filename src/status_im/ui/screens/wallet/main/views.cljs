(ns status-im.ui.screens.wallet.main.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [status-im.components.common.common :as common]
            [status-im.components.button.view :as btn]
            [status-im.components.drawer.view :as drawer]
            [status-im.components.list.views :as list]
            [status-im.components.react :as rn]
            [status-im.components.styles :as st]
            [status-im.components.icons.vector-icons :as vi]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.utils.config :as config]
            [status-im.utils.platform :as platform]
            [status-im.utils.utils :as utils]
            [status-im.ui.screens.wallet.main.styles :as wallet-styles]
            [status-im.utils.money :as money]))

(defn- show-not-implemented! []
  (utils/show-popup "TODO" "Not implemented yet!"))

(defn toolbar-title []
  [rn/touchable-highlight {:on-press #(rf/dispatch [:navigate-to :wallet-list])
                           :style wallet-styles/toolbar-title-container}
   [rn/view {:style wallet-styles/toolbar-title-inner-container}
    [rn/text {:style wallet-styles/toolbar-title-text
              :font  :toolbar-title}
     (i18n/label :t/main-wallet)]
    [vi/icon
     :icons/dropdown
     {:container-style wallet-styles/toolbar-title-icon
      :color :white}]]])

(def transaction-history-action
  {:icon      :icons/transaction-history
   :icon-opts (merge {:color :white :style {:viewBox "-108 65.9 24 24"}} wallet-styles/toolbar-icon)
   :handler   #(rf/dispatch [:navigate-to-modal :wallet-transactions])})

(defn toolbar-view []
  [toolbar/toolbar2 {:style wallet-styles/toolbar}
   [toolbar/nav-button (act/hamburger-white drawer/open-drawer)]
   [toolbar-title]
   [toolbar/actions
    [(assoc (act/opts [{:text (i18n/label :t/wallet-settings) :value show-not-implemented!}]) :icon-opts {:color :white})
     transaction-history-action]]])

(defn error-message-view [error-message]
  [rn/view {:style wallet-styles/wallet-error-container}
   [rn/view {:style wallet-styles/wallet-exclamation-container}
    [vi/icon :icons/exclamation_mark {:color           :white
                                      :container-style wallet-styles/wallet-error-exclamation}]]
   [rn/text {:style wallet-styles/wallet-error-message} (i18n/label :t/wallet-error)]])

(defn main-section [usd-value change error-message]
  [rn/view {:style wallet-styles/main-section}
   (when error-message [error-message-view error-message])
   [rn/view {:style wallet-styles/total-balance-container}
    [rn/view {:style wallet-styles/total-balance}
     [rn/text {:style wallet-styles/total-balance-value} usd-value]
     [rn/text {:style wallet-styles/total-balance-currency} "USD"]]
    [rn/view {:style wallet-styles/value-variation}
     [rn/text {:style wallet-styles/value-variation-title}
      (i18n/label :t/wallet-total-value)]
     [rn/view {:style (if (pos? change) wallet-styles/today-variation-container-positive wallet-styles/today-variation-container-negative)}
      [rn/text {:style (if (pos? change) wallet-styles/today-variation-positive wallet-styles/today-variation-negative)}
       change]]]
    [btn/buttons wallet-styles/buttons
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
    "eth" {:source (:ethereum resources/assets) :style (wallet-styles/asset-border st/color-gray-transparent-light)}))

(defn render-assets-fn [{:keys [id currency amount]}]
  ;; TODO(jeluard) Navigate to asset details screen
  #_
  [list/touchable-item show-not-implemented!
   [rn/view
    [list/item
     [list/item-image {:uri :launch_logo}]
     [rn/view {:style wallet-styles/asset-item-value-container}
      [rn/text {:style wallet-styles/asset-item-value} (str amount)]
      [rn/text {:style      wallet-styles/asset-item-currency
                :uppercase? true}
       id]]
     [list/item-icon {:style :icons/forward}]]]]
  [rn/view
   [list/item
    (let [{:keys [source style]} (token->image id)]
      [list/item-image source style])
    [rn/view {:style wallet-styles/asset-item-value-container}
     [rn/text {:style wallet-styles/asset-item-value} (str amount)]
     [rn/text {:style      wallet-styles/asset-item-currency
               :uppercase? true}
      id]]]])

(defn render-add-asset-fn [{:keys [id currency amount]}]
  [list/touchable-item show-not-implemented!
   [rn/view
    [list/item
     [list/item-icon {:icon :icons/add :style wallet-styles/add-asset-icon :icon-opts {:color :blue}}]
     [rn/view {:style wallet-styles/asset-item-value-container}
      [rn/text {:style wallet-styles/add-asset-text}
       (i18n/label :t/wallet-add-asset)]]]]])

(defn asset-section [eth prices-loading? balance-loading?]
  (let [assets [{:id "eth" :currency :eth :amount eth}]]
    [rn/view {:style wallet-styles/asset-section}
     [rn/text {:style wallet-styles/asset-section-title} (i18n/label :t/wallet-assets)]
     [list/section-list
      {:sections                 [{:key        :assets
                                   :data       assets
                                   :renderItem  (list/wrap-render-fn render-assets-fn)}
                                  {:key        :add-asset
                                   :data       [{}]
                                   :renderItem (list/wrap-render-fn render-add-asset-fn)}]
       :render-section-header-fn #()
       :on-refresh               #(rf/dispatch [:refresh-wallet])
       :refreshing               (or prices-loading? balance-loading?)}]]))

(defview wallet []
  (letsubs [eth-balance [:eth-balance]
            portfolio-value [:portfolio-value]
            portfolio-change [:portfolio-change]
            prices-loading?    [:prices-loading?]
            balance-loading?   [:wallet/balance-loading?]
            error-message [:wallet/error-message]]
    [rn/view {:style wallet-styles/wallet-container}
     [toolbar-view]
     [rn/scroll-view
      [main-section portfolio-value portfolio-change error-message]
      [asset-section eth-balance prices-loading? balance-loading?]]]))
