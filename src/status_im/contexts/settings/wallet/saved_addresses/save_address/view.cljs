(ns status-im.contexts.settings.wallet.saved-addresses.save-address.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.contexts.settings.wallet.saved-addresses.save-address.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn view
  []
  (let [{:keys [edit?]} (rf/sub [:get-screen-params])
        {:keys [address name customization-color ens ens?]}
        (rf/sub [:wallet/saved-address])
        [address-label set-address-label] (rn/use-state (or name ""))
        [address-color set-address-color] (rn/use-state (or customization-color
                                                            (rand-nth colors/account-colors)))
        placeholder (i18n/label :t/address-name)
        address-text (rn/use-callback
                      (fn []
                        [quo/address-text
                         {:full-address? true
                          :address       address
                          :format        :long}])
                      [address])
        on-press-save (rn/use-callback
                       (fn []
                         (rf/dispatch [:wallet/save-address
                                       {:on-success
                                        (if edit?
                                          [:wallet/edit-saved-address-success]
                                          [:wallet/add-saved-address-success
                                           (i18n/label :t/address-saved)])
                                        :on-error
                                        [:wallet/add-saved-address-failed]
                                        :name address-label
                                        :ens (when ens? ens)
                                        :address address
                                        :customization-color address-color}]))
                       [address address-label
                        address-color])
        data-item-props (rn/use-memo
                         #(cond-> {:status          :default
                                   :size            :default
                                   :subtitle-type   :default
                                   :label           :none
                                   :blur?           true
                                   :card?           true
                                   :title           (i18n/label :t/address)
                                   :subtitle        ens
                                   :custom-subtitle address-text
                                   :container-style style/data-item}
                            ens?
                            (dissoc :custom-subtitle))
                         [ens ens? address-text])]
    [quo/overlay {:type :shell}
     [floating-button-page/view
      {:footer-container-padding     (if edit? (+ (safe-area/get-bottom) 12) 0)
       :keyboard-should-persist-taps :handled
       :header                       [quo/page-nav
                                      {:type                :no-title
                                       :background          :blur
                                       :icon-name           (if edit? :i/close :i/arrow-left)
                                       :on-press            navigate-back
                                       :margin-top          (when-not edit? (safe-area/get-top))
                                       :accessibility-label :save-address-page-nav}]
       :footer                       [quo/button
                                      {:accessibility-label :save-address-button
                                       :type                :primary
                                       :customization-color address-color
                                       :disabled?           (string/blank? address-label)
                                       :on-press            on-press-save}
                                      (i18n/label :t/save-address)]
       :customization-color          address-color
       :gradient-cover?              true
       :shell-overlay?               true}
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
        :auto-focus          (not edit?)
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
      [quo/data-item data-item-props]]]))
