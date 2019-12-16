(ns status-im.ui.screens.wallet.send.sheets
  (:require-macros [status-im.utils.views :refer [defview letsubs] :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.screens.wallet.accounts.views :as wallet.accounts]
            [status-im.utils.utils :as utils.utils]))

(views/defview assets [address]
  (views/letsubs [{:keys [tokens]} [:wallet/visible-assets-with-values address]
                  currency [:wallet/currency]]
    [list/flat-list
     {:data      tokens
      :key-fn    (comp str :symbol)
      :render-fn (wallet.accounts/render-asset
                  (:code currency)
                  #(re-frame/dispatch [:wallet.send/set-symbol (:symbol %)]))}]))

(defn render-account [field]
  (fn [account]
    [list-item/list-item
     {:icon     [chat-icon/custom-icon-view-list (:name account) (:color account)]
      :title    (:name account)
      :on-press #(re-frame/dispatch [:wallet.send/set-field field account])}]))

(views/defview accounts-list [field]
  (views/letsubs [accounts [:multiaccount/accounts]
                  accounts-whithout-watch [:accounts-without-watch-only]]
    [list/flat-list {:data      (if (= :to field) accounts accounts-whithout-watch)
                     :key-fn    :address
                     :render-fn (render-account field)}]))

(defn- request-camera-permissions []
  (let [options {:handler        :wallet.send/qr-scanner-result
                 :cancel-handler :wallet.send/qr-scanner-cancel}]
    (re-frame/dispatch
     [:request-permissions
      {:permissions [:camera]
       :on-allowed #(re-frame/dispatch [:wallet.send/qr-scanner-allowed options])
       :on-denied
       #(utils.utils/set-timeout
         (fn []
           (utils.utils/show-popup (i18n/label :t/error)
                                   (i18n/label :t/camera-access-error)))
         50)}])))

(defn show-accounts-list []
  (re-frame/dispatch [:bottom-sheet/hide-sheet])
  (js/setTimeout #(re-frame/dispatch [:bottom-sheet/show-sheet
                                      {:content        (fn [] [accounts-list :to])
                                       :content-height 300}]) 400))

(defn choose-recipient []
  [react/view
   (for [item [{:title    (i18n/label :t/accounts)
                :icon     :main-icons/profile
                :theme    :action
                :accessibility-label :chose-recipient-accounts-button
                :on-press show-accounts-list}
               {:title    (i18n/label :t/scan-qr)
                :icon     :main-icons/qr
                :theme    :action
                :accessibility-label :chose-recipient-scan-qr
                :on-press request-camera-permissions}
               {:title    (i18n/label :t/recipient-code)
                :icon     :main-icons/address
                :theme    :action
                :accessibility-label :choose-recipient-recipient-code
                :on-press #(re-frame/dispatch [:wallet.send/navigate-to-recipient-code])}]]
     ^{:key item}
     [list-item/list-item item])])
