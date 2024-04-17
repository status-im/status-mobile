(ns status-im.contexts.wallet.save-address.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.utils :as wallet-common-utils]
    [status-im.contexts.wallet.save-address.style :as style]
    [status-im.contexts.wallet.sheets.network-preferences.view
     :as network-preferences]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- address-view
  [{:keys [selected-networks set-selected-networks]}]
  (let [address (rf/sub [:wallet/wallet-send-to-address])]
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
                         (fn []
                           [quo/address-text
                            {:networks selected-networks
                             :address  address
                             :format   :long}])
                         [selected-networks])
       :on-press        (rn/use-callback
                         (fn []
                           (rf/dispatch
                            [:show-bottom-sheet
                             {:content
                              (fn []
                                [network-preferences/view
                                 {:blur              false
                                  :selected-networks (->> selected-networks
                                                          (map :network-name)
                                                          set)
                                  :account           {:address address}
                                  :button-label      (i18n/label :t/save)
                                  :on-save           (rn/use-callback
                                                      (fn [chain-ids]
                                                        (set-selected-networks
                                                         (map wallet-common-utils/id->network chain-ids))
                                                        (rf/dispatch [:hide-bottom-sheet])))}
                                ])}]))
                         [selected-networks])
       :container-style style/data-item}]]))

(defn view
  []
  (let [address (rf/sub [:wallet/wallet-send-to-address])
        saved-address (rf/sub [:wallet/saved-address-by-address address])
        [address-label set-address-label] (rn/use-state (:name saved-address))
        [address-color set-address-color] (rn/use-state (if (seq (:colorId saved-address))
                                                          (-> saved-address :colorId keyword)
                                                          :blue))
        [selected-networks set-selected-networks]
        (rn/use-state
         (when (seq (:chainShortNames saved-address))
           (wallet-common-utils/network-preference-prefix->network-names (:chainShortNames
                                                                          saved-address))))
        selected-networks-or-fallback (map (fn [network-name]
                                             {:network-name network-name
                                              :short-name   (wallet-common-utils/network->short-name
                                                             network-name)})
                                           (if (seq selected-networks)
                                             selected-networks
                                             constants/default-network-names))
        placeholder (i18n/label :t/address-name)]
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
                                  :type :primary
                                  :container-style style/save-address-button
                                  :on-press
                                  (rn/use-callback
                                   (fn []
                                     (rf/dispatch
                                      [:wallet/save-address
                                       {:address address
                                        :name address-label
                                        :customization-color address-color
                                        :on-success (fn [] (js/alert "Address Saved"))
                                        :chain-short-names
                                        (wallet-common-utils/short-names->network-preference-prefix
                                         (map :short-name selected-networks-or-fallback))}]))
                                   [address address-label address-color selected-networks-or-fallback])}
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
     [address-view
      {:selected-networks     selected-networks-or-fallback
       :set-selected-networks set-selected-networks}]]))

(comment
  (rf/dispatch [:open-modal :screen/wallet.save-address])
  (rf/dispatch [:navigate-to :screen/wallet.transaction-progress])

  (contains? [:a :b :c] 5)
  (some #(= :a %) [:a :b])


  (list :main :arb)



  (rf/dispatch [:wallet/get-saved-addresses])

  (do
    (rf/dispatch [:profile/on-password-input-changed {:password "      "}])
    (rf/dispatch [:profile.login/login])

    (rf/dispatch [:wallet-temp/set-to-address
                  "0x26fe3219384a55e4e89fdb0e4420b15439221428"])
    )
  )
