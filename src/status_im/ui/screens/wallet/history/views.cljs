(ns status-im.ui.screens.wallet.history.views
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [status-im.components.react :as rn]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.ui.screens.wallet.history.styles :as st]
            [status-im.i18n :as i18n]))

(defn unsigned-action []
  [rn/view {:style st/toolbar-buttons-container}
   [rn/text (i18n/label :t/transactions-sign-all)]])

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
  [rn/view {:style st/item}

   [rn/image {:source {:uri :console}
              :style  st/item-icon}]
#_
   [rn/icon :dropdown-white #_(icon-status (:status item)) st/item-icon]

   [rn/view {:style st/item-text-view}
    (let [m (:content item)]
      [rn/text {:style st/primary-text} (str (:value m) " " (:symbol m))])
    [rn/text {:style st/secondary-text} (:to item)]]
   [rn/icon :forward_gray st/secondary-action]])

(defn main-section []
  [rn/view {:style st/main-section}
   (rn/flat-list [{:to "0xAAAAA"   :content {:value "5" :symbol "ETH"} :status :pending}
                  {:from "0xAAAAA" :content {:value "5" :symbol "ETH"} :status :sent}
                  {:to "0xAAAAA"   :content {:value "5" :symbol "ETH"} :status :pending}] render-transaction)])

(defview wallet-transactions []
  []
  [rn/view {:style st/wallet-transactions-container}
   [toolbar-view]
   [rn/scroll-view
    [main-section]]])
