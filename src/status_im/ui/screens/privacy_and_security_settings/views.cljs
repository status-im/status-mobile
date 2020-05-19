(ns status-im.ui.screens.privacy-and-security-settings.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [quo.core :as quo]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.react :as react]
            [status-im.multiaccounts.biometric.core :as biometric]
            [status-im.ui.components.topbar :as topbar]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.views :as views]))

(views/defview privacy-and-security []
  (views/letsubs [{:keys [mnemonic preview-privacy?]} [:multiaccount]
                  supported-biometric-auth [:supported-biometric-auth]
                  auth-method              [:auth-method]]
    [react/view {:flex 1 :background-color colors/white}
     [topbar/topbar {:title :t/privacy-and-security}]
     [react/scroll-view {:padding-vertical 8}
      [quo/list-header (i18n/label :t/security)]
      [quo/list-item {:size                :small
                      :title               (i18n/label :t/back-up-seed-phrase)
                      :accessibility-label :back-up-recovery-phrase-button
                      :disabled            (not mnemonic)
                      :chevron             (boolean mnemonic)
                      :accessory           (when mnemonic [components.common/counter {:size 22} 1])
                      :on-press            #(re-frame/dispatch [:navigate-to :backup-seed])}]
      (when supported-biometric-auth
        [quo/list-item
         {:size                :small
          :title               (str (i18n/label :t/lock-app-with) " " (biometric/get-label supported-biometric-auth))
          :active              (= auth-method "biometric")
          :accessibility-label :biometric-auth-settings-switch
          :accessory           :switch
          :on-press            #(re-frame/dispatch [:multiaccounts.ui/biometric-auth-switched
                                                    ((complement boolean) (= auth-method "biometric"))])}])
      [react/view {:margin-vertical  8
                   :background-color colors/gray-lighter
                   :height           1}]
      ;; TODO - uncomment when implemented
      ;; {:size       :small
      ;;  :title       (i18n/label :t/change-password)
      ;;  :chevron true}
      ;; {:size                   :small
      ;;  :title                   (i18n/label :t/change-passcode)
      ;;  :chevron true}

      [quo/list-header (i18n/label :t/privacy)]
      [quo/list-item {:size                :small
                      :title               (i18n/label :t/set-dapp-access-permissions)
                      :on-press            #(re-frame/dispatch [:navigate-to :dapps-permissions])
                      :accessibility-label :dapps-permissions-button
                      :chevron             true}]
      [quo/list-item {:size                    :small
                      :title                   (if platform/android?
                                                 (i18n/label :t/hide-content-when-switching-apps)
                                                 (i18n/label :t/hide-content-when-switching-apps-ios))
                      :container-margin-bottom 8
                      :active                  preview-privacy?
                      :accessory               :switch
                      :on-press                #(re-frame/dispatch
                                                 [:multiaccounts.ui/preview-privacy-mode-switched
                                                  ((complement boolean) preview-privacy?)])}]

      (comment
        {:container-margin-top 8
         :size                 :small
         :title                :t/delete-my-account
         :theme                :negative})]]))
