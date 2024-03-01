(ns status-im.contexts.wallet.send.save-address.view
  (:require
   [quo.core :as quo]
   [re-frame.core :as rf]
   [react-native.core :as rn]
   [status-im.contexts.wallet.send.save-address.style :as style]
   [utils.i18n :as i18n]))

(defn view []
  (let [[address-label set-address-label] (rn/use-state "")
        [address-color set-address-color] (rn/use-state :blue)]
    [:<>
     [quo/page-nav
      {:type                :no-title
       :background          :blur
       :icon-name           :i/close
       :on-press            #(rf/dispatch [:navigate-back])
       :accessibility-label :save-address-top-bar}]

     [rn/view
      {:style style/account-avatar-container}
      [quo/user-avatar
       {:full-name           address-label
        :customization-color address-color
        :size                :big}]]

     [quo/title-input
      {:blur?               true
       :max-length          24
       :size                :heading-1
       :placeholder         (i18n/label :t/address-name)
       :default-value       address-label
       :on-change-text      set-address-label
       :customization-color address-color
       :container-style     style/title-input-container}]

     [quo/divider-line {:container-style style/divider-1}]

     [quo/section-label
      {:section         (i18n/label :t/colour)
       :container-style style/section-container}]

     [quo/color-picker
      {:default-selected address-color
       :on-change        set-address-color
       :container-style  style/color-picker}]

     [quo/divider-line {:container-style style/divider-2}]

     [quo/button
      {:accessibility-label :save-address-button
       :type                :primary
       :container-style     style/save-address-button}
      (i18n/label :t/save-address)]]))
