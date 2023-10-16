(ns status-im.ui.screens.wallet.accounts-manage.views
  (:require
    [reagent.core :as reagent]
    [status-im.ui.components.colors :as colors]
    [status-im.ui.components.icons.icons :as icons]
    [status-im.ui.components.list.item :as list.item]
    [status-im.ui.components.list.views :as list]
    [status-im.utils.utils :as utils]
    [utils.re-frame :as rf]))

(defn render-account
  [_]
  (reagent/create-class
   {:should-component-update
    (fn [_ [_ old-item] [_ new-item]]
      (not= (:hidden old-item) (:hidden new-item)))
    :reagent-render
    (fn [{:keys [hidden name address wallet] :as account}]
      [list.item/list-item
       {:accessory           [icons/icon
                              (if hidden :main-icos/hide :main-icos/show)
                              (merge {:accessibility-label (if hidden :hide-icon :show-icon)}
                                     (when wallet {:color colors/gray}))]
        :animated-accessory? false
        :animated            false
        :disabled            wallet
        :title               name
        :subtitle            (utils/get-shortened-checksum-address address)
        :on-press            #(rf/dispatch [:wallet.accounts/save-account account
                                            {:hidden (not hidden)}])}])}))

(defn manage
  []
  (let [accounts (rf/sub [:profile/wallet-accounts])]
    [list/flat-list
     {:key-fn    :address
      :data      accounts
      :render-fn render-account}]))
