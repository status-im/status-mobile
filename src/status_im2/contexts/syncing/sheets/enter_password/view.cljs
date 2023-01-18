(ns status-im2.contexts.syncing.sheets.enter-password.view
  (:require [clojure.string :as string]
            [utils.i18n :as i18n]
            [quo.core :as quo-old]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.constants :as constants]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [utils.re-frame :as rf]))

(defn qr-code-view-with-connection-string
  [connection-string]
  (let [window-width                (rf/sub [:dimensions/window-width])
        eighty-percent-screen-width (* window-width 0.8)
        valid-cs?                   (string/starts-with?
                                     connection-string
                                     constants/local-pairing-connection-string-identifier)]
    [:<>
     (if valid-cs?
       [rn/view {:margin 20}
        [quo/text
         {:accessibility-label :sync-code-generated
          :weight              :bold
          :size                :heading-1
          :style               {:color  colors/neutral-100
                                :margin 20}}
         (i18n/label :t/sync-code-generated)]
        [qr-code-viewer/qr-code-view eighty-percent-screen-width connection-string]
        [quo/information-box
         {:type      :informative
          :closable? false
          :icon      :i/placeholder
          :style     {:margin-top 20}} (i18n/label :t/instruction-after-qr-generated)]]
       [rn/view {:margin 20}
        [rn/view {:padding-horizontal 8}
         [quo/button
          {:on-press #(rf/dispatch [:preparations-for-connection-string])}
          (i18n/label :t/try-your-luck-again)]]])]))

(defn sheet
  []
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
         [quo-old/text-input ;;TODO : migrate text-input from quo to quo2 namespace
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
         {:on-press #(rf/dispatch [:syncing/get-connection-string-for-bootstrapping-another-device
                                   @entered-password])}
         (i18n/label :t/generate-scan-sync-code)]]]]]))
