(ns status-im.contexts.settings.wallet.network-settings.testnet-mode.view
  (:require [quo.core :as quo]
            [status-im.contexts.settings.wallet.network-settings.testnet-mode.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn hide-bottom-sheet
  []
  (rf/dispatch [:hide-bottom-sheet]))

(defn toggle-testnet-mode
  []
  (hide-bottom-sheet)
  (rf/dispatch [:wallet/toggle-testnet-mode]))

(defn testnet-mode-confirm-change-sheet
  [{:keys [title blur? description]}]
  (let [customization-color (rf/sub [:profile/customization-color])]
    [:<>
     [quo/drawer-top
      {:title           title
       :blur?           blur?
       :container-style style/drawer-top}]
     [quo/text {:style style/description} description]
     [quo/bottom-actions
      {:container-style  {:style style/bottom-actions-container}
       :actions          :two-actions
       :blur?            blur?
       :button-one-label (i18n/label :t/confirm)
       :button-one-props {:accessibility-label :confirm-testnet-mode-change
                          :on-press            toggle-testnet-mode
                          :type                :primary
                          :customization-color customization-color}
       :button-two-label (i18n/label :t/cancel)
       :button-two-props {:accessibility-label :cancel-testnet-mode-change
                          :type                :grey
                          :on-press            hide-bottom-sheet}}]]))

(defn view
  [{:keys [blur?]}]
  (let [testnet-mode? (rf/sub [:profile/test-networks-enabled?])]
    (if (not testnet-mode?)
      [testnet-mode-confirm-change-sheet
       {:blur?       blur?
        :title       (i18n/label :t/turn-on-testnet-mode)
        :description (i18n/label :t/testnet-mode-enable-description)}]
      [testnet-mode-confirm-change-sheet
       {:blur?       blur?
        :title       (i18n/label :t/turn-off-testnet-mode)
        :description (i18n/label :t/testnet-mode-disable-description)}])))
