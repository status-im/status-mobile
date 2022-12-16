(ns status-im.ui.screens.wallet.accounts-manage.views
  (:require [status-im.utils.handlers :refer [>evt <sub]]
            [status-im.ui.components.list.views :as list]
            [reagent.core :as reagent]
            [quo.core :as quo]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.icons.icons :as icons]
            [quo.design-system.colors :as colors]))

(defn render-account [_]
  (reagent/create-class
   {:should-component-update
    (fn [_ [_ old-item] [_ new-item]]
      (not= (:hidden old-item) (:hidden new-item)))
    :reagent-render
    (fn [{:keys [hidden name address wallet] :as account}]
      [quo/list-item
       {:accessory           [icons/icon
                              (if hidden :main-icos/hide :main-icos/show)
                              (merge {:accessibility-label (if hidden :hide-icon :show-icon)}
                                     (when wallet {:color colors/gray}))]
        :animated-accessory? false
        :animated            false
        :disabled            wallet
        :title               name
        :subtitle            (utils/get-shortened-checksum-address address)
        :on-press            #(>evt [:wallet.accounts/save-account account {:hidden (not hidden)}])}])}))

(defn manage []
  (let [accounts (<sub [:multiaccount/accounts])]
    [list/flat-list
     {:key-fn    :address
      :data      accounts
      :render-fn render-account}]))
