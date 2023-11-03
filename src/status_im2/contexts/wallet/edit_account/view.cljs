(ns status-im2.contexts.wallet.edit-account.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.wallet.common.screen-base.create-or-edit-account.view :as
             create-or-edit-account]
            [status-im2.contexts.wallet.common.sheets.network-preferences.view :as network-preferences]
            [status-im2.contexts.wallet.edit-account.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn show-toast
  [{:keys [type theme]}]
  (let [messsage (condp = type
                   :name  :t/edit-wallet-account-name-updated-message
                   :color :t/edit-wallet-account-colour-updated-message
                   :emoji :t/edit-wallet-account-emoji-updated-message)]
    (rf/dispatch [:toasts/upsert
                  {:id         :edit-account
                   :icon       :i/correct
                   :icon-color (colors/resolve-color :success theme)
                   :text       (i18n/label messsage)}])))

(defn- view-internal
  [{:keys [theme]}]
  (let [{:keys [name customization-color emoji address]
         :as   account}      (rf/sub [:wallet/current-viewing-account])
        edited-data          (reagent/atom {:name                name
                                            :customization-color customization-color
                                            :emoji               emoji})
        account-name         (reagent/cursor edited-data [:name])
        account-color        (reagent/cursor edited-data [:customization-color])
        account-emoji        (reagent/cursor edited-data [:emoji])
        on-change-name       (fn [edited-name]
                               ;;validation needs to be performed on the name (#17372)
                               (swap! edited-data assoc :name edited-name))
        show-confirm-button? (reagent/atom false)
        on-change-color      (fn [edited-color]
                               (swap! edited-data assoc :customization-color edited-color)
                               (rf/dispatch [:wallet/save-account
                                             {:address     address
                                              :edited-data @edited-data}
                                             #(show-toast {:type  :color
                                                           :theme theme})]))
        on-change-emoji      (fn [edited-emoji]
                               (swap! edited-data assoc :emoji edited-emoji)
                               (rf/dispatch [:wallet/save-account
                                             {:address     address
                                              :edited-data @edited-data}
                                             #(show-toast {:type  :emoji
                                                           :theme theme})]))
        on-confirm           (fn []
                               (rn/dismiss-keyboard!)
                               (rf/dispatch [:wallet/save-account
                                             {:address     address
                                              :edited-data @edited-data}
                                             #(show-toast {:type  :name
                                                           :theme theme})]))]
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
        :section-label       :t/account-info
        :on-focus            #(reset! show-confirm-button? true)
        :on-blur             #(reset! show-confirm-button? false)
        :bottom-action?      @show-confirm-button?
        :bottom-action-label :t/update-account-name
        :bottom-action-props {:customization-color @account-color
                              :disabled?           (= name @account-name)
                              :on-press            on-confirm}}
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
         :custom-subtitle (fn [] [quo/address-text
                                  {:networks [{:network-name :ethereum :short-name "eth"}
                                              {:network-name :optimism :short-name "opt"}
                                              {:network-name :arbitrum :short-name "arb1"}]
                                   :address  address
                                   :format   :long}])
         :on-press        (fn []
                            (rf/dispatch [:show-bottom-sheet
                                          {:content (fn [] [network-preferences/view
                                                            {:account account
                                                             :on-save #(js/alert
                                                                        "calling on save")}])}]))
         :container-style style/data-item}]])))

(def view (quo.theme/with-theme view-internal))
