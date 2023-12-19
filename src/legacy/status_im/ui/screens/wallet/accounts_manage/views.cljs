(ns legacy.status-im.ui.screens.wallet.accounts-manage.views
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.list.views :as list]
    [legacy.status-im.utils.utils :as utils]
    [reagent.core :as reagent]
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
        :on-press            #(rf/dispatch [:wallet-legacy.accounts/save-account account
                                            {:hidden (not hidden)}])}])}))

(defn manage
  []
  (let [accounts (rf/sub [:profile/wallet-accounts])]
    [list/flat-list
     {:key-fn    :address
      :data      accounts
      :render-fn render-account}]))
