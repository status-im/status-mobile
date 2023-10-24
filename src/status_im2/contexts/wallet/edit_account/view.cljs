(ns status-im2.contexts.wallet.edit-account.view
  (:require [quo.core :as quo]
            [quo.theme :as quo.theme]
            [reagent.core :as reagent]
            [status-im2.contexts.wallet.common.network-preferences.view :as network-preferences]
            [status-im2.contexts.wallet.common.screen-base.create-or-edit-account.view :as
             create-or-edit-account]
            [status-im2.contexts.wallet.edit-account.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- view-internal
  []
  (let [account-name    (reagent/atom "Account 1")
        account-color   (reagent/atom :purple)
        account-emoji   (reagent/atom "🍑")
        account-address "0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045"
        on-change-name  #(reset! account-name %)
        on-change-color #(reset! account-color %)
        on-change-emoji #(reset! account-emoji %)]
    (fn []
      [create-or-edit-account/view
       {:page-nav-right-side [{:icon-name :i/delete
                               :on-press  #(js/alert "Delete account: to be implemented")}]
        :account-name        @account-name
        :account-emoji       @account-emoji
        :account-color       @account-color
        :on-change-name      on-change-name
        :on-change-color     on-change-color
        :on-change-emoji     on-change-emoji
        :section-label       :t/account-info}
       [quo/data-item
        {:status          :default
         :size            :default
         :description     :default
         :label           :none
         :blur?           false
         :icon-right?     true
         :right-icon      :i/advanced
         :card?           true
         :title           (i18n/label :t/address)
         :subtitle        account-address
         :on-press        (fn []
                            (rf/dispatch [:show-bottom-sheet
                                          {:content (fn [] [network-preferences/view
                                                            {:address account-address
                                                             :on-save #(js/alert
                                                                        "calling on save")}])}]))
         :container-style style/data-item}]])))

(def view (quo.theme/with-theme view-internal))
