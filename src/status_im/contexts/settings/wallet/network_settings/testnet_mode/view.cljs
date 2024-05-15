(ns status-im.contexts.settings.wallet.network-settings.testnet-mode.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.contexts.settings.wallet.network-settings.testnet-mode.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn hide-bottom-sheet
  []
  (rf/dispatch [:hide-bottom-sheet]))

(defn logout
  []
  (rf/dispatch [:logout]))

(defn on-confirm-change
  [enable?]
  (hide-bottom-sheet)
  (rf/dispatch [:profile.settings/profile-update :test-networks-enabled? enable? {:on-success logout}]))

(defn testnet-mode-confirm-change-sheet
  [{:keys [title description on-confirm on-cancel]}]
  (let [customization-color (rf/sub [:profile/customization-color])]
    [:<>
     [quo/drawer-top
      {:title           title
       :container-style style/drawer-top}]
     [quo/text {:style style/description} description]
     [rn/view {:style style/info-box-container}
      [quo/information-box
       {:type :default
        :icon :i/info}
       (i18n/label :t/change-testnet-mode-logout-info)]]
     [quo/bottom-actions
      {:container-style  {:style style/bottom-actions-container}
       :actions          :two-actions
       :button-one-label (i18n/label :t/confirm)
       :button-one-props {:accessibility-label :confirm-testnet-mode-change
                          :on-press            on-confirm
                          :type                :primary
                          :customization-color customization-color}
       :button-two-label (i18n/label :t/cancel)
       :button-two-props {:accessibility-label :cancel-testnet-mode-change
                          :type                :dark-grey
                          :on-press            on-cancel}}]]))

(defn view
  [{:keys [enable?]}]
  [:<>
   (if enable?
     [testnet-mode-confirm-change-sheet
      {:title       (i18n/label :t/turn-on-testnet-mode)
       :description (i18n/label :t/testnet-mode-enable-description)
       :on-confirm  #(on-confirm-change enable?)
       :on-cancel   hide-bottom-sheet}]
     [testnet-mode-confirm-change-sheet
      {:title       (i18n/label :t/turn-off-testnet-mode)
       :description (i18n/label :t/testnet-mode-disable-description)
       :on-confirm  #(on-confirm-change enable?)
       :on-cancel   hide-bottom-sheet}])])
