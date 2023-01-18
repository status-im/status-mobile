(ns status-im.ui.screens.multiaccounts.recover.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [utils.i18n :as i18n]
            [status-im.keycard.recovery :as keycard]
            [status-im.multiaccounts.key-storage.core :as multiaccounts.key-storage]
            [status-im.multiaccounts.recover.core :as multiaccounts.recover]
            [status-im.qr-scanner.core :as qr-scanner]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im2.config :as config]
            [utils.security.core]))

(defn hide-sheet-and-dispatch
  [event]
  (re-frame/dispatch [:bottom-sheet/hide])
  (re-frame/dispatch event))

(defview custom-seed-phrase
  []
  [react/view
   [react/view {:margin-top 24 :margin-horizontal 24 :align-items :center}
    [react/view
     {:width           32
      :height          32
      :border-radius   16
      :align-items     :center
      :justify-content :center}
     [icons/icon :main-icons/help {:color colors/blue}]]
    [react/text
     {:style {:typography    :title-bold
              :margin-top    8
              :margin-bottom 8}}
     (i18n/label :t/custom-seed-phrase)]
    [react/view
     {:flex-wrap       :wrap
      :flex-direction  :row
      :justify-content :center
      :text-align      :center}
     [react/nested-text
      {:style {:color       colors/gray
               :text-align  :center
               :line-height 22}}
      (i18n/label :t/custom-seed-phrase-text-1)]]
    [react/view
     {:margin-vertical 24
      :align-items     :center}
     [quo/button
      {:on-press            #(re-frame/dispatch [:hide-popover])
       :accessibility-label :cancel-custom-seed-phrase
       :type                :secondary}
      (i18n/label :t/cancel)]]]])

(defview bottom-sheet-view
  []
  (letsubs [view-id                      [:view-id]
            acc-to-login-keycard-pairing [:intro-wizard/acc-to-login-keycard-pairing]]
    [react/view {:flex-direction :row}
     [react/view {}
      ;; Show manage storage link when on login screen, only on android devices
      ;; and the selected account is not paired with keycard
      (when (and (= view-id :login)
                 (not acc-to-login-keycard-pairing))
        [quo/list-item
         {:theme               :accent
          :title               (i18n/label :t/manage-keys-and-storage)
          :accessibility-label :manage-keys-and-storage-button
          :icon                :main-icons/key
          :on-press            #(hide-sheet-and-dispatch
                                 [::multiaccounts.key-storage/key-and-storage-management-pressed])}])

      [quo/list-item
       {:theme               :accent
        :title               (i18n/label :t/recover-with-seed-phrase)
        :accessibility-label :enter-seed-phrase-button
        :icon                :main-icons/text
        :on-press            #(hide-sheet-and-dispatch [::multiaccounts.recover/enter-phrase-pressed])}]
      [quo/list-item
       {:theme               :accent
        :title               (i18n/label :t/recover-with-keycard)
        :accessibility-label :recover-with-keycard-button
        :icon                [react/view
                              {:border-width     1
                               :border-radius    20
                               :border-color     colors/blue-light
                               :background-color colors/blue-light
                               :justify-content  :center
                               :align-items      :center
                               :width            40
                               :height           40}
                              [react/image
                               {:source (resources/get-image :keycard-logo-blue)
                                :style  {:width 24 :height 24}}]]
        :on-press            #(hide-sheet-and-dispatch [::keycard/recover-with-keycard-pressed])}]
      (when config/database-management-enabled?
        [quo/list-item
         {:theme    :accent
          :on-press #(hide-sheet-and-dispatch [:multiaccounts.login.ui/import-db-submitted])
          :icon     :main-icons/receive
          :title    "Import unencrypted"}])
      (when config/database-management-enabled?
        [quo/list-item
         {:theme    :accent
          :on-press #(hide-sheet-and-dispatch [:multiaccounts.login.ui/export-db-submitted])
          :icon     :main-icons/send
          :title    "Export unencrypted"}])
      (when config/local-pairing-mode-enabled?
        [:<>
         [quo/list-item
          {:theme    :accent
           :on-press #(hide-sheet-and-dispatch [::qr-scanner/scan-code
                                                {:handler ::qr-scanner/on-scan-success}])
           :icon     :i/key
           :title    (i18n/label :t/scan-sync-code)}]
         [quo/list-item
          {:theme    :accent
           :on-press #(hide-sheet-and-dispatch [:navigate-to :multiaccounts])
           :icon     :i/key
           :title    (i18n/label :t/show-existing-keys)}]])]]))

(def bottom-sheet
  {:content bottom-sheet-view})

(comment
  ;; Recover with seed to device UI flow
  (do

    ;; Goto seed screen
    (re-frame/dispatch [::multiaccounts.recover/enter-phrase-pressed])

    ;; Enter seed phrase for Dim Venerated Yaffle
    (re-frame/dispatch
     [:multiaccounts.recover/enter-phrase-input-changed
      (utils.security.core/mask-data
       "rocket mixed rebel affair umbrella legal resemble scene virus park deposit cargo")])

    ;; Recover multiaccount
    (re-frame/dispatch [:multiaccounts.recover/enter-phrase-next-pressed])

    ;; Press Re-encrypt
    (re-frame/dispatch [:multiaccounts.recover/re-encrypt-pressed])

    ;; Press next on default storage (ie store on device)
    (re-frame/dispatch [:multiaccounts.recover/select-storage-next-pressed])

    ;; Enter password (need to wait for a moment for this to finish)
    (re-frame/dispatch [:multiaccounts.recover/enter-password-next-pressed "111111"])))
