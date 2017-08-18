(ns status-im.ui.screens.wallet.main.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [status-im.components.common.common :as common]
            [status-im.components.button.view :as btn]
            [status-im.components.drawer.view :as drawer]
            [status-im.components.react :as rn]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.i18n :as i18n]
            [status-im.utils.listview :as lw]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.wallet.main.styles :as st]
            [status-im.utils.money :as money]))

(defn toolbar-title []
  [rn/view {:style st/toolbar-title-container}
   [rn/text {:style st/toolbar-title-text
             :font  :toolbar-title}
    "Main Wallet"]
   [rn/icon :dropdown_white st/toolbar-title-icon]])

(defn toolbar-buttons []
  [rn/view {:style st/toolbar-buttons-container}
   [rn/icon :dots_vertical_white st/toolbar-icon]
   [rn/icon :qr_white st/toolbar-icon]])

(defn toolbar-view []
  [toolbar/toolbar {:style          st/toolbar
                    :nav-action     (act/list-white #(rf/dispatch [:navigate-to-modal :wallet-transactions]))
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
     [{:text     "Send"
       :on-press #(rf/dispatch [:navigate-to :wallet-send-transaction])}
      {:text     "Request"
       :on-press #(rf/dispatch [:navigate-to :wallet-request-transaction])}
      {:text      "Exchange"
       :disabled? true}]]]])

(defn asset-list-item [[id {:keys [currency amount] :as row}]]
  [rn/view {:style st/asset-item-container}
   [rn/image {:source {:uri :launch_logo}
              :style  st/asset-item-currency-icon}]
   [rn/view {:style st/asset-item-value-container}
    [rn/text {:style st/asset-item-value} (str amount)]
    [rn/text {:style      st/asset-item-currency
              :uppercase? true}
     id]]
   [rn/icon :forward_gray st/asset-item-details-icon]])

(defn render-separator-fn [assets-count]
  (fn [_ row-id _]
    (rn/list-item
     ^{:key row-id}
     [common/separator {} st/asset-list-separator])))

(defn render-row-fn [row _ _]
  (rn/list-item
   [rn/touchable-highlight {:on-press #()}
    [rn/view
     [asset-list-item row]]]))

(def assets-example-map
  {"eth" {:currency :eth :amount 0.445}
   "snt" {:currency :snt :amount 1}
   "gno" {:currency :gno :amount 0.024794}})

;; NOTE(oskarth): In development, replace assets with assets-example-map
;; to check multiple assets being rendered
(defn asset-section [eth]
  (let [assets {"eth" {:currency :eth :amount eth}}]
    [rn/view {:style st/asset-section}
     [rn/text {:style st/asset-section-title} "Assets"]
     [rn/list-view {:dataSource      (lw/to-datasource assets)
                    :renderSeparator (when platform/ios? (render-separator-fn (count assets)))
                    :renderRow       render-row-fn}]]))

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
