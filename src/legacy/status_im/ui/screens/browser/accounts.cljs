(ns legacy.status-im.ui.screens.browser.accounts
  (:require
    [legacy.status-im.ui.components.chat-icon.screen :as chat-icon]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.list.views :as list]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.utils.utils :as utils]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n]))

(defn render-account
  [account _ _ dapps-account]
  [list.item/list-item
   (merge {:accessory :radio
           :active    (= (:address dapps-account) (:address account))
           :icon      [chat-icon/custom-icon-view-list (:name account) (:color account)]
           :title     (:name account)
           :subtitle  (utils/get-shortened-checksum-address (:address account))}
          (when (not= (:address dapps-account) (:address account))
            {:on-press #(re-frame/dispatch [:dapps-account-selected (:address account)])}))])

(defn accounts-list
  [accounts dapps-account]
  (fn []
    [react/view {:flex 1}
     [react/text {:style {:margin 16 :text-align :center}}
      (i18n/label :t/select-account-dapp)]
     [list/flat-list
      {:data        accounts
       :key-fn      :address
       :render-data dapps-account
       :render-fn   render-account}]]))
