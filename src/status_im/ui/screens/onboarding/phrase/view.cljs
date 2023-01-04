(ns status-im.ui.screens.onboarding.phrase.view
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.onboarding.views :as ui]
            [utils.datetime :as datetime]
            [status-im.utils.utils :as utils]
            [utils.debounce :refer [dispatch-and-chill]]
            [utils.security.core :as security]))

(defview wizard-recovery-success
  []
  (letsubs [{:keys [pubkey processing? name identicon]} [:intro-wizard/recovery-success]
            existing-account?                           [:intro-wizard/recover-existing-account?]]
    [react/view
     {:style {:flex            1
              :justify-content :space-between}}
     [ui/title-with-description :t/keycard-recovery-success-header :t/recovery-success-text]
     [react/view
      {:flex             1
       :justify-content  :space-between
       :background-color colors/white}
      [react/view
       {:flex            1
        :justify-content :space-between
        :align-items     :center}
       [react/view
        {:flex-direction  :column
         :flex            1
         :justify-content :center
         :align-items     :center}
        [react/view
         {:margin-horizontal 16
          :flex-direction    :column}
         [react/view
          {:justify-content :center
           :align-items     :center
           :margin-bottom   11}
          [react/image
           {:source {:uri identicon}
            :style  {:width         61
                     :height        61
                     :border-radius 30
                     :border-width  1
                     :border-color  colors/black-transparent}}]]
         [react/text
          {:style           {:text-align  :center
                             :color       colors/black
                             :font-weight "500"}
           :number-of-lines 1
           :ellipsize-mode  :middle}
          name]
         [quo/text
          {:style           {:margin-top 4}
           :monospace       true
           :color           :secondary
           :align           :center
           :number-of-lines 1
           :ellipsize-mode  :middle}
          (utils/get-shortened-address pubkey)]]]]]
     [ui/next-button
      #(dispatch-and-chill [:multiaccounts.recover/re-encrypt-pressed] 300)
      (or processing? existing-account?)]]))

(defn enter-phrase
  [_]
  (let [show-bip39-password? (reagent/atom false)
        pressed-in-at        (atom nil)]
    (fn []
      (let [{:keys [processing?
                    passphrase-word-count
                    next-button-disabled?
                    passphrase-error]}
            @(re-frame/subscribe [:intro-wizard/enter-phrase])]
        [react/keyboard-avoiding-view {:flex 1}
         [quo/text
          {:weight :bold
           :align  :center
           :size   :x-large}
          (i18n/label :t/multiaccounts-recover-enter-phrase-title)]
         [react/pressable
          {:style        {:background-color   colors/white
                          :flex               1
                          :justify-content    :center
                          :padding-horizontal 16}
           ;; BIP39 password input will be shown only after pressing on screen
           ;; for longer than 2 seconds
           :on-press-in  (fn []
                           (reset! pressed-in-at (datetime/now)))
           :on-press-out (fn []
                           (when (>= (datetime/seconds-ago @pressed-in-at) 2)
                             (reset! show-bip39-password? true)))}
          [react/view
           [quo/text-input
            {:on-change-text      #(re-frame/dispatch [:multiaccounts.recover/enter-phrase-input-changed
                                                       (security/mask-data %)])
             :auto-focus          true
             :error               (when passphrase-error (i18n/label passphrase-error))
             :accessibility-label :passphrase-input
             :placeholder         (i18n/label :t/seed-phrase-placeholder)
             :show-cancel         false
             :bottom-value        40
             :multiline           true
             :auto-correct        false
             :keyboard-type       :visible-password
             :monospace           true}]
           [react/view {:align-items :flex-end}
            [react/view
             {:flex-direction   :row
              :align-items      :center
              :padding-vertical 8
              :opacity          (if passphrase-word-count 1 0)}
             [quo/text
              {:color (if next-button-disabled? :secondary :main)
               :size  :small}
              (when-not next-button-disabled?
                "✓ ")
              (i18n/label-pluralize passphrase-word-count :t/words-n)]]]
           (when @show-bip39-password?
             ;; BIP39 password (`passphrase` in BIP39
             ;; https://github.com/bitcoin/bips/blob/master/bip-0039.mediawiki#from-mnemonic-to-seed)
             ;; is an advanced security feature which allows to add an arbitrary
             ;; extra word to your existing mnemonic. The password is an empty
             ;; string if not provided. If the password is added a completely
             ;; different key will be created.
             [quo/text-input
              {:on-change-text
               #(re-frame/dispatch [:multiaccounts.recover/enter-passphrase-input-changed
                                    (security/mask-data %)])
               :placeholder    (i18n/label :t/bip39-password-placeholder)
               :show-cancel    false}])]]
         [react/view {:align-items :center}
          [react/text
           {:style {:color         colors/gray
                    :font-size     14
                    :margin-bottom 8
                    :text-align    :center}}
           (i18n/label :t/multiaccounts-recover-enter-phrase-text)]
          (when processing?
            [react/view {:flex 1 :align-items :center}
             [react/activity-indicator
              {:size      :large
               :animating true}]
             [react/text
              {:style {:color      colors/gray
                       :margin-top 8}}
              (i18n/label :t/processing)]])]
         [ui/next-button
          #(dispatch-and-chill [:multiaccounts.recover/enter-phrase-next-pressed] 300)
          next-button-disabled?]]))))
