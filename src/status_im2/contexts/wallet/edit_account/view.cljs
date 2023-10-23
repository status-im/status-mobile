(ns status-im2.contexts.wallet.edit-account.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [quo.foundations.resources :as resources]
            [quo.theme :as quo.theme]
            [reagent.core :as reagent]
            [status-im2.contexts.wallet.common.screen-base.create-or-edit-account.view :as
             create-or-edit-account]
            [status-im2.contexts.wallet.edit-account.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(def account-address "0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045")

(def mainnet
  [{:title        "Mainnet"
    :image        :icon-avatar
    :image-props  {:icon (resources/get-network :ethereum)
                   :size :size-20}
    :action       :selector
    :action-props {:type :checkbox}}])

(def networks-list
  [{:title        "Optimism"
    :image        :icon-avatar
    :image-props  {:icon (resources/get-network :optimism)
                   :size :size-20}
    :action       :selector
    :action-props {:type :checkbox}}
   {:title        "Arbitrum"
    :image        :icon-avatar
    :image-props  {:icon (resources/get-network :arbitrum)
                   :size :size-20}
    :action       :selector
    :action-props {:type :checkbox}}])

(defn network-preferences
  []
  [:<>
   [quo/drawer-top
    {:title       (i18n/label :t/network-preferences)
     :description (i18n/label :t/network-preferences-desc)}]
   [quo/data-item
    {:status          :default
     :size            :default
     :description     :default
     :label           :none
     :blur?           false
     :card?           true
     :title           (i18n/label :t/address)
     :subtitle        account-address
     :container-style (merge style/data-item
                             {:background-color (colors/theme-colors colors/neutral-2_5
                                                                     colors/neutral-90)})}]
   [quo/category
    {:list-type :settings
     :data      mainnet}]
   [quo/category
    {:list-type :settings
     :label     (i18n/label :t/layer-2)
     :data      networks-list}]
   [quo/bottom-actions
    {:button-one-label (i18n/label :t/update)
     :disabled?        true}]])

(defn- view-internal
  []
  (let [account-name    (reagent/atom "Account 1")
        account-color   (reagent/atom :purple)
        account-emoji   (reagent/atom "🍑")
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
         :on-press        #(rf/dispatch [:show-bottom-sheet {:content network-preferences}])
         :container-style style/data-item}]])))

(def view (quo.theme/with-theme view-internal))
