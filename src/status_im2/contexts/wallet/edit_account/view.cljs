(ns status-im2.contexts.wallet.edit-account.view
  (:require [quo2.core :as quo]
            [quo2.theme :as quo.theme]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [status-im2.contexts.wallet.edit-account.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- view-internal
  []
  (let [{:keys [top]}   (safe-area/get-insets)
        account-name    "Account 1"
        account-color   (reagent/atom :purple)
        account-address "0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045"
        account-emoji   (reagent/atom "🍑")]
    (fn []
      [rn/keyboard-avoiding-view
       {:style (style/root-container top)}
       [quo/page-nav
        {:type       :no-title
         :background :blur
         :right-side [{:icon-name :i/delete
                       :on-press  #(js/alert "Delete account: to be implemented")}]
         :icon-name  :i/close
         :on-press   #(rf/dispatch [:navigate-back])}]
       [quo/gradient-cover
        {:customization-color @account-color
         :container-style     (style/gradient-cover-container top)}]
       [rn/scroll-view {:bounces false}
        [rn/view {:style style/account-avatar-container}
         [quo/account-avatar
          {:customization-color @account-color
           :size                80
           :emoji               @account-emoji
           :type                :default}]
         [quo/button
          {:size            32
           :type            :grey
           :background      :photo
           :icon-only?      true
           :on-press        #(rf/dispatch [:emoji-picker/open
                                           {:on-select (fn [selected-emoji]
                                                         (reset! account-emoji selected-emoji))}])
           :container-style style/reaction-button-container}
          :i/reaction]]
        [quo/title-input
         {:placeholder     (i18n/label :t/account-name-input-placeholder)
          :max-length      24
          :blur?           true
          :default-value   account-name
          :container-style style/title-input-container}]
        [quo/divider-line {:container-style style/divider-1}]
        [quo/section-label
         {:section         (i18n/label :t/colour)
          :container-style style/section-container}]
        [rn/view {:style style/color-picker-container}
         [quo/color-picker
          {:default-selected @account-color
           :on-change        #(reset! account-color %)
           :container-style  style/color-picker}]]
        [quo/divider-line {:container-style style/divider-2}]
        [quo/section-label
         {:section         (i18n/label :t/account-info)
          :container-style style/section-container}]
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
          :on-press        #(js/alert "Network selector: to be implemented")
          :container-style style/data-item}]]])))

(def view (quo.theme/with-theme view-internal))
