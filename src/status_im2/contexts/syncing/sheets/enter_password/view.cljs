(ns status-im2.contexts.syncing.sheets.enter-password.view
  (:require [utils.i18n :as i18n]
            [quo.core :as quo-old]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [utils.re-frame :as rf]))

;;TODO : this file is temporary and will be removed for new design auth method
(defn sheet
  [set-code]
  (let [entered-password (atom "")]
    [:<>
     [rn/view {:margin 20}
      [rn/view
       [quo/text
        {:accessibility-label :sync-code-generated
         :weight              :bold
         :size                :heading-1
         :style               {:color  colors/neutral-100
                               :margin 20}}
        (i18n/label :t/enter-your-password)]
       [rn/view {:flex-direction :row :align-items :center}
        [rn/view {:flex 1}
         [quo-old/text-input
          {:placeholder         (i18n/label :t/enter-your-password)
           :auto-focus          true
           :accessibility-label :password-input
           :show-cancel         false
           :on-change-text      #(reset! entered-password %)
           :secure-text-entry   true}]]]
       [rn/view
        {:padding-horizontal 18
         :margin-top         20}
        [quo/button
         {:on-press (fn []
                      ;TODO https://github.com/status-im/status-mobile/issues/15570
                      ;remove old bottom sheet when Authentication process design is created.
                      (rf/dispatch [:bottom-sheet/hide-old])
                      (rf/dispatch [:syncing/get-connection-string-for-bootstrapping-another-device
                                    @entered-password set-code]))}
         (i18n/label :t/generate-scan-sync-code)]]]]]))
