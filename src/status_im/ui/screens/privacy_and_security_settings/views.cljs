(ns status-im.ui.screens.privacy-and-security-settings.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [quo.core :as quo]
            [status-im.multiaccounts.reset-password.core :as reset-password]
            [status-im.multiaccounts.key-storage.core :as key-storage]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.react :as react]
            [status-im.utils.config :as config]
            [status-im.multiaccounts.biometric.core :as biometric]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.views :as views]))

(defn separator []
  [quo/separator {:style {:margin-vertical  8}}])

(views/defview privacy-and-security []
  (views/letsubs [{:keys [mnemonic
                          preview-privacy?
                          messages-from-contacts-only
                          webview-allow-permission-requests?]} [:multiaccount]
                  supported-biometric-auth [:supported-biometric-auth]
                  keycard?                 [:keycard-multiaccount?]
                  auth-method              [:auth-method]]
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
     [quo/list-item {:size                :small
                     :title               (i18n/label :t/chat-link-previews)
                     :chevron             true
                     :on-press            #(re-frame/dispatch [:navigate-to :link-previews-settings])
                     :accessibility-label :chat-link-previews}]
     [quo/list-item {:size                :small
                     :title               (i18n/label :t/accept-new-chats-from)
                     :chevron             true
                     :accessory           :text
                     :accessory-text      (i18n/label (if messages-from-contacts-only
                                                        :t/contacts
                                                        :t/anyone))
                     :on-press            #(re-frame/dispatch [:navigate-to :messages-from-contacts-only])
                     :accessibility-label :accept-new-chats-from}]
     (when (not keycard?)
       [quo/list-item {:size                :small
                       :title               (i18n/label :t/reset-password)
                       :chevron             true
                       :accessory           :text
                       :on-press            #(do
                                               (re-frame/dispatch [::reset-password/clear-form-vals])
                                               (re-frame/dispatch [:navigate-to :reset-password]))
                       :accessibility-label :reset-password}])
     (when config/metrics-enabled?
       [quo/list-item {:size                :small
                       :title               (i18n/label :t/anonymous-usage-data)
                       :chevron             true
                       :on-press            #(re-frame/dispatch [:navigate-to :anonymous-metrics-settings])
                       :accessibility-label :anonymous-usage-data}])
     (when platform/android?
       [quo/list-item {:size               :small
                       :title              (i18n/label :t/webview-camera-permission-requests)
                       :active             webview-allow-permission-requests?
                       :accessory          :switch
                       :subtitle           (i18n/label :t/webview-camera-permission-requests-subtitle)
                       :subtitle-max-lines 2
                       :on-press           #(re-frame/dispatch
                                             [:multiaccounts.ui/webview-permission-requests-switched
                                              ((complement boolean) webview-allow-permission-requests?)])}])
     (when (not keycard?)
       [quo/list-item {:size                :small
                       :title               (i18n/label :t/manage-keys-and-storage)
                       :chevron             true
                       :on-press            #(re-frame/dispatch [::key-storage/logout-and-goto-key-storage])
                       :accessibility-label :key-managment}])
     [separator]
     [quo/list-item
      {:size                :small
       :theme               :negative
       :title               (i18n/label :t/delete-my-profile)
       :on-press            #(re-frame/dispatch [:navigate-to :delete-profile])
       :accessibility-label :dapps-permissions-button
       :chevron             true}]]))
