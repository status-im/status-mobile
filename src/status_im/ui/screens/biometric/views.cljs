(ns status-im.ui.screens.biometric.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.button :as button]
            [re-frame.core :as re-frame]
            [status-im.multiaccounts.biometric.core :as biometric]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.i18n :as i18n]))

(views/defview enable-biometric-popover []
  (views/letsubs [supported-biometric-auth [:supported-biometric-auth]]
    (let [bio-type-label (biometric/get-label supported-biometric-auth)]
      [react/view {:padding 24 :align-items :center}
       [react/view {:margin-bottom 16 :width 32 :height 32 :background-color colors/blue-light
                    :border-radius 16 :align-items :center :justify-content :center}
        [icons/icon (if (= supported-biometric-auth :FaceID) :faceid :print)]]
       [react/text {:style {:typography :title-bold}} (str (i18n/label :t/enable) " " bio-type-label)]
       [react/text {:style {:margin-bottom 25 :margin-top 10 :text-align :center}}
        (i18n/label :t/to-enable-biometric {:bio-type-label bio-type-label})]
       [button/button {:label (i18n/label :t/ok-save-pass) :style {:margin-bottom 16}
                       :on-press #(re-frame/dispatch [:biometric-logout])}]
       [button/button {:label :t/cancel :type :secondary
                       :on-press #(re-frame/dispatch [:hide-popover])}]])))