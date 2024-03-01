(ns status-im.contexts.wallet.send.save-address.view
  (:require
   [quo.core :as quo]
   [react-native.core :as rn]
   [status-im.contexts.wallet.common.sheets.network-preferences.view
    :as network-preferences]
   [status-im.contexts.wallet.send.save-address.style :as style]
   [utils.i18n :as i18n]
   [utils.re-frame :as rf]))

(defn- address-view [address]
  (let [network-details (rf/sub [:wallet/network-preference-details])]
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
      :custom-subtitle (fn [] [quo/address-text
                              {:networks network-details
                               :address  address
                               :format   :long}])
      :on-press        (fn []
                         (rf/dispatch [:show-bottom-sheet
                                       {:content
                                        (fn []
                                          [network-preferences/view
                                           {:on-save (fn [])
                                            ;; (fn [chain-ids]
                                            ;;   (rf/dispatch [:hide-bottom-sheet])
                                            ;;   (save-account
                                            ;;    {:account     account
                                            ;;     :updated-key network-preferences-key
                                            ;;     :new-value   chain-ids}))
                                            ;; :watch-only? watch-only?
                                            }])}]))
      :container-style style/data-item}])
  )
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

     [address-view "0xshivek"]
     [quo/button
      {:accessibility-label :save-address-button
       :type                :primary
       :container-style     style/save-address-button}
      (i18n/label :t/save-address)]]))
