(ns status-im.contexts.wallet.add-address-to-watch.confirm-address.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.emoji-picker.utils :as emoji-picker.utils]
    [status-im.contexts.wallet.add-address-to-watch.confirm-address.style :as style]
    [status-im.contexts.wallet.common.screen-base.create-or-edit-account.view :as
     create-or-edit-account]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [{:keys [address]}  (rf/sub [:get-screen-params])
        number-of-accounts (count (rf/sub [:profile/wallet-accounts]))
        account-name       (reagent/atom (i18n/label :t/default-account-name
                                                     {:number (inc number-of-accounts)}))
        account-color      (reagent/atom (rand-nth colors/account-colors))
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
         :watch-only?         true
         :bottom-action?      true
         :bottom-action-label :t/add-watched-address
         :bottom-action-props {:customization-color @account-color
                               :disabled?           (string/blank? @account-name)
                               :on-press            #(rf/dispatch [:wallet/add-account
                                                                   {:sha3-pwd     nil
                                                                    :type         :watch
                                                                    :account-name @account-name
                                                                    :emoji        @account-emoji
                                                                    :color        @account-color}
                                                                   {:address    address
                                                                    :public-key ""}])}}
        [quo/data-item
         {:card?           true
          :right-icon      :i/advanced
          :icon-right?     true
          :emoji           @account-emoji
          :title           (i18n/label :t/watched-address)
          :subtitle        address
          :status          :default
          :size            :default
          :subtitle-type   :default
          :custom-subtitle (fn [] [quo/text
                                   {:size   :paragraph-2
                                    ;; TODO: monospace font
                                    ;; https://github.com/status-im/status-mobile/issues/17009
                                    :weight :monospace}
                                   address])
          :container-style style/data-item
          :on-press        #(js/alert "To be implemented")}]]])))
