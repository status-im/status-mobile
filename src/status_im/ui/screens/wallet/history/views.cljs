(ns status-im.ui.screens.wallet.history.views
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [status-im.components.button.view :as btn]
            [status-im.components.react :as rn]
            [status-im.components.list.styles :as list-st]
            [status-im.components.list.views :as list]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.ui.screens.wallet.history.styles :as st]
            [status-im.i18n :as i18n]))

(defn unsigned-action []
  [rn/text {:style st/toolbar-right-action}
   (i18n/label :t/transactions-sign-all)])

(defn toolbar-view []
  [toolbar/toolbar
   {:title         (i18n/label :t/transactions)
    :title-style   {:text-align "center"}
    :custom-action [unsigned-action]}])

(defn- icon-status [k]
  (case k
    :pending :dropdown_white
    :dropdown_white))

(defn render-transaction
  [item]
  [rn/view {:style list-st/item}
   [rn/image {:source {:uri :console}
              :style  list-st/item-icon}]
   [rn/view {:style list-st/item-text-view}
    (let [m (:content item)]
      [rn/text {:style list-st/primary-text} (str (:value m) " " (:symbol m))])
    [rn/text {:style list-st/secondary-text} (str (i18n/label :t/transactions-to) " " (:to item))]
    [rn/view {:style list-st/action-buttons}
     [btn/primary-button {:text (i18n/label :t/transactions-sign)}]
     [btn/secondary-button {:text (i18n/label :t/transactions-delete)}]]]
   [rn/icon :forward_gray list-st/secondary-action]])

(def dummy-transaction-data
  [{:to "0xAAAAA" :content {:value "0,4909" :symbol "ETH"}}
   {:to "0xAAAAA" :content {:value "10000" :symbol "SGT"}}])

(defn main-section []
  [rn/view {:style st/main-section}
   [list/flat-list dummy-transaction-data render-transaction]])

;; TODO must reflect selected wallet

(defview wallet-transactions []
  []
  [rn/view {:style st/wallet-transactions-container}
   [toolbar-view]
   [rn/scroll-view
    [main-section]]])
