(ns status-im.ui.screens.wallet.main-screen.views
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [status-im.components.common.common :as common]
            [status-im.components.drawer.view :as drawer]
            [status-im.components.react :as rn]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.i18n :as i18n]
            [status-im.utils.listview :as lw]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.wallet.main-screen.styles :as st]))

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

;; TODO: Use standard signature and move to action-button namespace
(defn action-button [{:keys [on-press view-style text-style text]}]
  [rn/touchable-highlight {:on-press on-press}
   [rn/view {:style view-style}
    [rn/text {:style      text-style
              :font       :medium
              :uppercase? false} text]]])

;; TODO: button to go to send screen
(defn action-buttons []
  [rn/view {:style st/action-buttons-container}
   [action-button {:on-press #(rf/dispatch [:navigate-to :wallet-send-transaction])
                   :view-style st/action-button
                   :text-style st/action-button-text
                   :text "Send"}]
   [action-button {:on-press #(rf/dispatch [:navigate-to :wallet-request-transaction])
                   :view-style st/action-button-center
                   :text-style st/action-button-text
                   :text "Request"}]
   [action-button {:on-press (fn [] )
                   :view-style st/action-button
                   :text-style st/action-button-text-disabled
                   :text "Exchange"}]])

(defn main-section []
  [rn/view {:style st/main-section}
   [rn/view {:style st/total-balance-container}
    [rn/view {:style st/total-balance}
     [rn/text {:style st/total-balance-value} "12.43"]
     [rn/text {:style st/total-balance-currency} "USD"]]
    [rn/view {:style st/value-variation}
     [rn/text {:style st/value-variation-title} "Total value"]
     [rn/view {:style st/today-variation-container}
      [rn/text {:style st/today-variation} "+5.43%"]]]
    [action-buttons]]])

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

(defn asset-section []
  (let [assets {"eth" {:currency :eth :amount 0.445}
                "snt" {:currency :snt :amount 1}
                "gno" {:currency :gno :amount 0.024794}}]
    [rn/view {:style st/asset-section}
     [rn/text {:style st/asset-section-title} "Assets"]
     [rn/list-view {:dataSource      (lw/to-datasource assets)
                    :renderSeparator (when platform/ios? (render-separator-fn (count assets)))
                    :renderRow       render-row-fn}]]))

(defview wallet []
  []
  [rn/view {:style st/wallet-container}
   [toolbar-view]
   [rn/scroll-view
    [main-section]
    [asset-section]]])
