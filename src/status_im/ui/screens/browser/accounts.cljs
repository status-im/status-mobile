(ns status-im.ui.screens.browser.accounts
  (:require [status-im.ui.components.list.views :as list]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.utils.utils :as utils]
            [re-frame.core :as re-frame]))

(defn render-account [dapps-account]
  (fn [account]
    [list-item/list-item
     {:theme     :selectable
      :selected? (= (:address dapps-account) (:address account))
      :icon      [chat-icon/custom-icon-view-list (:name account) (:color account)]
      :title     (:name account)
      :subtitle  (utils/get-shortened-checksum-address (:address account))
      :on-press  #(re-frame/dispatch [:dapps-account-selected (:address account)])}]))

(defn accounts-list [accounts dapps-account]
  (fn []
    [react/view {:flex 1}
     [react/text {:style {:margin 16 :text-align :center}}
      (i18n/label :t/select-account-dapp)]
     [list/flat-list {:data      accounts
                      :key-fn    :address
                      :render-fn (render-account dapps-account)}]]))