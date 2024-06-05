(ns status-im.contexts.settings.wallet.saved-addresses.save-address.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.common.not-implemented :as not-implemented]
    [status-im.constants :as constants]
    [status-im.contexts.settings.wallet.saved-addresses.save-address.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn- extract-address
  [address]
  (re-find constants/regx-address-contains address))

(defn view
  []
  (let [{:keys [address]}                 (rf/sub [:wallet/saved-address])
        [address-label set-address-label] (rn/use-state "")
        [address-color set-address-color] (rn/use-state (rand-nth colors/account-colors))
        placeholder                       (i18n/label :t/address-name)
        on-press-save                     (rn/use-callback
                                           (fn []
                                             (let [address-without-prefix (extract-address address)]
                                               (rf/dispatch [:wallet/save-address
                                                             {:on-success
                                                              [:wallet/add-saved-address-success
                                                               (i18n/label :t/address-saved)]
                                                              :on-error
                                                              [:wallet/add-saved-address-failed]
                                                              :name address-label
                                                              :address address-without-prefix
                                                              :customization-color address-color}])))
                                           [address address-label address-color])]
    [quo/overlay {:type :shell}
     [floating-button-page/view
      {:footer-container-padding 0
       :header                   [quo/page-nav
                                  {:type                :no-title
                                   :background          :blur
                                   :icon-name           :i/arrow-left
                                   :on-press            navigate-back
                                   :margin-top          (safe-area/get-top)
                                   :accessibility-label :save-address-page-nav}]
       :footer                   [quo/button
                                  {:accessibility-label :save-address-button
                                   :type                :primary
                                   :customization-color address-color
                                   :disabled?           (string/blank? address-label)
                                   :on-press            on-press-save}
                                  (i18n/label :t/save-address)]
       :customization-color      address-color
       :gradient-cover?          true}
      [quo/wallet-user-avatar
       {:full-name           (if (string/blank? address-label)
                               placeholder
                               address-label)
        :customization-color address-color
        :blur?               true
        :size                :size-80
        :container-style     style/avatar}]
      [quo/title-input
       {:blur?               true
        :auto-focus          true
        :max-length          24
        :size                :heading-1
        :placeholder         placeholder
        :default-value       address-label
        :on-change-text      set-address-label
        :customization-color address-color
        :container-style     style/title-input}]
      [quo/divider-line
       {:blur?           true
        :container-style style/color-picker-top-divider}]
      [quo/section-label
       {:section         (i18n/label :t/colour)
        :blur?           true
        :container-style style/section-label}]
      [quo/color-picker
       {:default-selected address-color
        :on-change        set-address-color
        :blur?            true
        :container-style  style/color-picker}]
      [quo/divider-line
       {:blur?           true
        :container-style style/color-picker-bottom-divider}]
      [quo/data-item
       {:status          :default
        :size            :default
        :subtitle-type   :default
        :label           :none
        :blur?           true
        :icon-right?     true
        :right-icon      :i/advanced
        :card?           true
        :title           (i18n/label :t/address)
        :custom-subtitle (rn/use-callback
                          (fn []
                            [quo/address-text
                             {:full-address? true
                              :address       address
                              :format        :long}])
                          [address])
        :on-press        not-implemented/alert
        :container-style style/data-item}]]]))
