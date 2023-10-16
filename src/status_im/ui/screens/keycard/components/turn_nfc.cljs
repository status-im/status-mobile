(ns status-im.ui.screens.keycard.components.turn-nfc
  (:require
    [re-frame.core :as re-frame]
    [status-im.ui.components.colors :as colors]
    [status-im.ui.components.core :as quo]
    [status-im.ui.components.icons.icons :as icons]
    [status-im.ui.components.react :as react]
    [status-im.ui.screens.keycard.components.style :as styles]
    [utils.i18n :as i18n]))

(defn turn-nfc-on
  []
  [react/view {:style styles/wrapper-style}
   [react/view {:style styles/container-style}
    [icons/icon :main-icons/union-nfc
     {:color  colors/blue
      :height 36
      :width  36}]
    [react/view {:margin-top 16}
     [react/text {:style {:typography :title-bold}}
      (i18n/label :t/turn-nfc-on)]]
    [react/view
     {:margin-top    8
      :margin-bottom 16}
     [react/text
      {:number-of-lines 2
       :style           styles/helper-text-style}
      (i18n/label :t/turn-nfc-description)]]
    [quo/button {:on-press #(re-frame/dispatch [:keycard.onboarding.nfc-on/open-nfc-settings-pressed])}
     (i18n/label :t/open-nfc-settings)]]])
