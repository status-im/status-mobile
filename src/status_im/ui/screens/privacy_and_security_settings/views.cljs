(ns status-im.ui.screens.privacy-and-security-settings.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [quo.core :as quo]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.react :as react]
            [status-im.multiaccounts.biometric.core :as biometric]
            [status-im.ui.components.topbar :as topbar]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.views :as views]))

(defn separator []
  [quo/separator {:style {:margin-vertical  8}}])

(views/defview privacy-and-security []
  (views/letsubs [{:keys [mnemonic preview-privacy? webview-allow-permission-requests?]} [:multiaccount]
                  supported-biometric-auth [:supported-biometric-auth]
                  auth-method              [:auth-method]
                  keycard?                 [:keycard-multiaccount?]]
    [react/view {:flex 1 :background-color colors/white}
     [topbar/topbar {:title (i18n/label :t/privacy-and-security)}]
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
      [separator]
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
      [quo/list-item {:size                    :small
                      :title                   (i18n/label :t/chat-link-previews)
                      :chevron                 true
                      :on-press                #(re-frame/dispatch [:navigate-to :link-previews-settings])
                      :accessibility-label    :chat-link-previews}]
      (when platform/android?
        [quo/list-item {:size                    :small
                        :title                   (i18n/label :t/webview-camera-permission-requests)
                        :active                  webview-allow-permission-requests?
                        :accessory               :switch
                        :subtitle                (i18n/label :t/webview-camera-permission-requests-subtitle)
                        :subtitle-max-lines      2
                        :on-press                #(re-frame/dispatch
                                                   [:multiaccounts.ui/webview-permission-requests-switched
                                                    ((complement boolean) webview-allow-permission-requests?)])}])
      ;; TODO(rasom): remove this condition when kk support will be added
      (when-not keycard?
        [separator])
      (when-not keycard?
        [quo/list-item
         {:size                :small
          :theme               :negative
          :title               (i18n/label :t/delete-my-profile)
          :on-press            #(re-frame/dispatch [:navigate-to :delete-profile])
          :accessibility-label :dapps-permissions-button
          :chevron             true}])]]))
