(ns status-im2.contexts.wallet.add-watch-only-account.views
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [re-frame.core :as re-frame]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.contexts.emoji-picker.utils :as emoji-picker.utils]
    [status-im2.contexts.wallet.add-watch-only-account.style :as style]
    [status-im2.contexts.wallet.common.screen-base.create-or-edit-account.view :as
     create-or-edit-account]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- view-internal
  []
  (let [{:keys [address]}  (rf/sub [:get-screen-params])
        number-of-accounts (count (rf/sub [:profile/wallet-accounts]))
        account-name       (reagent/atom (i18n/label :t/default-account-name
                                                     {:number (inc number-of-accounts)}))
        address-title      (i18n/label :t/watch-address)
        account-color      (reagent/atom :blue)
        account-emoji      (reagent/atom (emoji-picker.utils/random-emoji))
        on-change-name     #(reset! account-name %)
        on-change-color    #(reset! account-color %)
        on-change-emoji    #(reset! account-emoji %)]
    (fn []
      [rn/view {:style style/container}
       [create-or-edit-account/view
        {:page-nav-right-side [{:icon-name :i/info
                                :on-press
                                #(js/alert
                                  "Get info (to be
                                    implemented)")}]
         :account-name        @account-name
         :account-emoji       @account-emoji
         :account-color       @account-color
         :on-change-name      on-change-name
         :on-change-color     on-change-color
         :on-change-emoji     on-change-emoji
         :bottom-action?      true
         :bottom-action-label :t/create-account
         :bottom-action-props {:customization-color @account-color
                               :disabled?           (string/blank? @account-name)
                               :on-press            #(re-frame/dispatch [:navigate-to
                                                                         :wallet-account])}}
        [quo/data-item
         {:card?           true
          :right-icon      :i/advanced
          :icon-right?     true
          :emoji           @account-emoji
          :title           address-title
          :subtitle        address
          :status          :default
          :size            :default
          :container-style style/data-item
          :on-press        #(js/alert "To be implemented")}]]])))

(def address-add-edit (quo.theme/with-theme view-internal))
