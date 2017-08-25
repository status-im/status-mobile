(ns status-im.ui.screens.wallet.main.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [status-im.components.common.common :as common]
            [status-im.components.button.view :as btn]
            [status-im.components.drawer.view :as drawer]
            [status-im.components.list.views :as list]
            [status-im.components.react :as rn]
            [status-im.components.icons.vector-icons :as vi]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.i18n :as i18n]
            [status-im.utils.config :as config]
            [status-im.utils.platform :as platform]
            [status-im.utils.utils :as utils]
            [status-im.ui.screens.wallet.main.styles :as st]
            [status-im.utils.money :as money]))

(defn- show-not-implemented! []
  (utils/show-popup "TODO" "Not implemented yet!"))

(defn toolbar-title []
  [rn/touchable-highlight {:on-press #(rf/dispatch [:navigate-to :wallet-list])
                           :style st/toolbar-title-container}
   [rn/view {:style st/toolbar-title-inner-container}
    [rn/text {:style st/toolbar-title-text
              :font  :toolbar-title}
     (i18n/label :t/main-wallet)]
    [vi/icon
     :icons/dropdown
     {:container-style st/toolbar-title-icon
      :color :white}]]])

(defn toolbar-buttons []
  [rn/view {:style st/toolbar-buttons-container}
   [vi/touchable-icon :icons/dots_vertical {:color :white} show-not-implemented!]
   [vi/touchable-icon :icons/qr {:color :white} show-not-implemented!]])

(defn- show-wallet-transactions []
  (if config/wallet-wip-enabled?
    (rf/dispatch [:navigate-to-modal :wallet-transactions])
    (show-not-implemented!)))

(defn toolbar-view []
  [toolbar/toolbar {:style          st/toolbar
                    :nav-action     (act/list-white show-wallet-transactions)
                    :custom-content [toolbar-title]
                    :custom-action  [toolbar-buttons]}])

;; TODO(oskarth): Whatever triggers the "in progress" animation should also trigger wallet-init or load-prices event.
(defn main-section [usd-value change]
  [rn/view {:style st/main-section}
   [rn/view {:style st/total-balance-container}
    [rn/view {:style st/total-balance}
     [rn/text {:style st/total-balance-value} usd-value]
     [rn/text {:style st/total-balance-currency} "USD"]]
    [rn/view {:style st/value-variation}
     [rn/text {:style st/value-variation-title} "Total value"]
     [rn/view {:style st/today-variation-container}
      [rn/text {:style st/today-variation} change]]]
    [btn/buttons st/buttons
     [{:text     (i18n/label :t/wallet-send)
       :on-press #(rf/dispatch [:navigate-to :wallet-send-transaction])
       :disabled? (not config/wallet-wip-enabled?)}
      {:text     (i18n/label :t/wallet-request)
       :on-press #(rf/dispatch [:navigate-to :wallet-request-transaction])
       :disabled? (not config/wallet-wip-enabled?)}
      {:text      (i18n/label :t/wallet-exchange)
       :disabled? true}]]]])

(defn render-asset-fn [{:keys [id currency amount]}]
  [list/touchable-item show-not-implemented!
   [rn/view
    [list/item
     [list/item-image {:uri :launch_logo}]
     [rn/view {:style st/asset-item-value-container}
      [rn/text {:style st/asset-item-value} (str amount)]
      [rn/text {:style      st/asset-item-currency
                :uppercase? true}
       id]]
     [list/item-icon :icons/forward]]]])

(defn asset-section [eth]
  (let [assets [{:id "eth" :currency :eth :amount eth}]]
    [rn/view {:style st/asset-section}
     [rn/text {:style st/asset-section-title} (i18n/label :t/wallet-assets)]
     [list/flat-list {:data      assets
                      :render-fn render-asset-fn}]]))

(defn eth-balance [{:keys [balance]}]
  (when balance
    (money/wei->ether balance)))

(defn portfolio-value [{:keys [balance]} {:keys [price]}]
  (when (and balance price)
    (-> (money/wei->ether balance)
        (money/eth->usd price)
        (money/with-precision 2)
        str)))

(defn portfolio-change [{:keys [price last-day]}]
  (when (and price last-day)
    (-> (money/percent-change price last-day)
        (money/with-precision 2)
        (str "%"))))

(defview wallet []
  (letsubs [wallet [:get :wallet]
            prices [:get :prices]]
    (let [eth    (or (eth-balance wallet) "...")
          usd    (or (portfolio-value wallet prices) "...")
          change (or (portfolio-change prices) "-%")]
      [rn/view {:style st/wallet-container}
       [toolbar-view]
       [rn/scroll-view
        [main-section usd change]
        [asset-section eth]]])))
