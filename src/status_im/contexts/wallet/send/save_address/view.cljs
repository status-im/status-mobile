(ns status-im.contexts.wallet.send.save-address.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.contexts.wallet.send.save-address.style :as style]
    [status-im.contexts.wallet.sheets.network-preferences.view
     :as network-preferences]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- address-view
  [address]
  (let [network-details (rf/sub [:wallet/network-preference-details])]
    [rn/view {:style style/address-container}
     [quo/data-item
      {:status          :default
       :size            :default
       :subtitle-type   :default
       :label           :none
       :blur?           false
       :icon-right?     true
       :right-icon      :i/advanced
       :card?           true
       :title           (i18n/label :t/address)
       :custom-subtitle (rn/use-callback
                         (fn [] [quo/address-text
                                 {:networks network-details
                                  :address  address
                                  :format   :long}]))
       :on-press        (rn/use-callback
                         (fn []
                           (rf/dispatch [:show-bottom-sheet
                                         {:content
                                          (fn []
                                            [network-preferences/view
                                             {:on-save (fn [])}])}])))
       :container-style style/data-item}]]))

(defn view
  []
  (let [[address-label set-address-label] (rn/use-state "")
        [address-color set-address-color] (rn/use-state :blue)
        placeholder                       (i18n/label :t/address-name)]
    [floating-button-page/view
     {:footer-container-padding 0
      :header                   [quo/page-nav
                                 {:type                :no-title
                                  :background          :blur
                                  :icon-name           :i/close
                                  :on-press            (rn/use-callback #(rf/dispatch [:navigate-back]))
                                  :accessibility-label :save-address-top-bar}]
      :footer                   [quo/button
                                 {:accessibility-label :save-address-button
                                  :type                :primary
                                  :container-style     style/save-address-button}
                                 (i18n/label :t/save-address)]

      :customization-color      address-color
      :gradient-cover?          true}

     [rn/view
      {:style style/account-avatar-container}
      [quo/user-avatar
       {:full-name           (if (string/blank? address-label)
                               placeholder
                               address-label)
        :customization-color address-color
        :size                :big}]]

     [quo/title-input
      {:blur?               true
       :auto-focus          true
       :max-length          24
       :size                :heading-1
       :placeholder         placeholder
       :default-value       address-label
       :on-change-text      set-address-label
       :customization-color address-color
       :container-style     style/title-input-container}]

     [quo/divider-line {:container-style style/color-picker-top-divider}]

     [quo/section-label
      {:section         (i18n/label :t/colour)
       :container-style style/section-container}]

     [quo/color-picker
      {:default-selected address-color
       :on-change        set-address-color
       :container-style  style/color-picker}]

     [quo/divider-line {:container-style style/color-picker-bottom-divider}]

     [address-view "0xshivek"]]))
