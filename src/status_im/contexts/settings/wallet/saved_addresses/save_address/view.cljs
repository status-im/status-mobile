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
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.common.utils.networks :as network-utils]
    [status-im.contexts.wallet.sheets.network-preferences.view :as network-preferences]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn- network-preferences-sheet
  [{:keys [address color selected-networks set-selected-networks]}]
  (fn []
    [network-preferences/view
     {:title             (i18n/label :t/add-network-preferences)
      :description       (i18n/label :t/saved-address-network-preference-selection-description)
      :button-label      (i18n/label :t/add-preferences)
      :blur?             true
      :selected-networks (set selected-networks)
      :account           {:address address
                          :color   color}
      :on-save           (fn [chain-ids]
                           (set-selected-networks (map network-utils/id->network chain-ids))
                           (rf/dispatch [:hide-bottom-sheet]))}]))

(defn view
  []
  (let [{:keys [edit?]} (rf/sub [:get-screen-params])
        {:keys [address name customization-color ens ens? network-preferences-names]}
        (rf/sub [:wallet/saved-address])
        [network-prefixes address-without-prefix] (utils/split-prefix-and-address address)
        [address-label set-address-label] (rn/use-state (or name ""))
        [address-color set-address-color] (rn/use-state (or customization-color
                                                            (rand-nth colors/account-colors)))
        [selected-networks set-selected-networks]
        (rn/use-state (or network-preferences-names
                          (network-utils/network-preference-prefix->network-names network-prefixes)))
        chain-short-names (rn/use-memo
                           #(network-utils/network-names->network-preference-prefix
                             selected-networks)
                           [selected-networks])
        placeholder (i18n/label :t/address-name)
        address-text (rn/use-callback
                      (fn []
                        [quo/address-text
                         {:full-address? true
                          :address       (str chain-short-names address-without-prefix)
                          :format        :long}])
                      [address-without-prefix chain-short-names])
        open-network-preferences (rn/use-callback
                                  (fn []
                                    (rf/dispatch
                                     [:show-bottom-sheet
                                      {:theme   :dark
                                       :shell?  true
                                       :content (network-preferences-sheet
                                                 {:address address-without-prefix
                                                  :color address-color
                                                  :selected-networks selected-networks
                                                  :set-selected-networks
                                                  set-selected-networks})}]))
                                  [address selected-networks address-color])
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
                                        :address address-without-prefix
                                        :customization-color address-color
                                        :chain-short-names chain-short-names}]))
                       [address-without-prefix chain-short-names address-label
                        address-color])
        data-item-props (rn/use-memo
                         #(cond-> {:status          :default
                                   :size            :default
                                   :subtitle-type   :default
                                   :label           :none
                                   :blur?           true
                                   :icon-right?     (not ens?)
                                   :right-icon      :i/advanced
                                   :card?           true
                                   :title           (i18n/label :t/address)
                                   :subtitle        ens
                                   :custom-subtitle address-text
                                   :on-press        open-network-preferences
                                   :container-style style/data-item}
                            ens?
                            (dissoc :custom-subtitle))
                         [ens ens? open-network-preferences address-text])]
    [quo/overlay {:type :shell}
     [floating-button-page/view
      {:footer-container-padding 0
       :header                   [quo/page-nav
                                  {:type                :no-title
                                   :background          :blur
                                   :icon-name           (if edit? :i/close :i/arrow-left)
                                   :on-press            navigate-back
                                   :margin-top          (when-not edit? (safe-area/get-top))
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
      [quo/data-item data-item-props]]]))
