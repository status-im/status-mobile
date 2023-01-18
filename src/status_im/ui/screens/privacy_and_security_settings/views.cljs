(ns status-im.ui.screens.privacy-and-security-settings.views
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [status-im2.setup.constants :as constants]
            [i18n.i18n :as i18n]
            [status-im.multiaccounts.biometric.core :as biometric]
            [status-im.multiaccounts.key-storage.core :as key-storage]
            [status-im.multiaccounts.reset-password.core :as reset-password]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.react :as react]
            [status-im.utils.config :as config]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.views :as views]))

(defn separator
  []
  [quo/separator {:style {:margin-vertical 8}}])

(def titles
  {constants/profile-pictures-visibility-contacts-only (i18n/label :t/recent-recipients)
   constants/profile-pictures-visibility-everyone      (i18n/label :t/everyone)
   constants/profile-pictures-visibility-none          (i18n/label :t/none)
   constants/profile-pictures-show-to-contacts-only    (i18n/label :t/recent-recipients)
   constants/profile-pictures-show-to-everyone         (i18n/label :t/everyone)
   constants/profile-pictures-show-to-none             (i18n/label :t/none)})

(views/defview privacy-and-security
  []
  (views/letsubs [{:keys [mnemonic
                          preview-privacy?
                          messages-from-contacts-only
                          webview-allow-permission-requests?
                          opensea-enabled?
                          profile-pictures-visibility]}
                  [:multiaccount]
                  has-picture [:profile/has-picture]
                  supported-biometric-auth [:supported-biometric-auth]
                  keycard? [:keycard-multiaccount?]
                  auth-method [:auth-method]
                  profile-pictures-show-to [:multiaccount/profile-pictures-show-to]]
    [react/scroll-view {:padding-vertical 8}
     [quo/list-header (i18n/label :t/security)]
     [quo/list-item
      {:size                :small
       :title               (i18n/label :t/back-up-seed-phrase)
       :accessibility-label :back-up-recovery-phrase-button
       :disabled            (not mnemonic)
       :chevron             (boolean mnemonic)
       :accessory           (when mnemonic [components.common/counter {:size 22} 1])
       :on-press            #(re-frame/dispatch [:navigate-to :backup-seed])}]
     (when supported-biometric-auth
       [quo/list-item
        {:size                :small
         :title               (str (i18n/label :t/lock-app-with)
                                   " "
                                   (biometric/get-label supported-biometric-auth))
         :active              (= auth-method "biometric")
         :accessibility-label :biometric-auth-settings-switch
         :accessory           :switch
         :on-press            #(re-frame/dispatch [:multiaccounts.ui/biometric-auth-switched
                                                   ((complement boolean)
                                                    (= auth-method "biometric"))])}])
     [separator]
     [quo/list-header (i18n/label :t/privacy)]
     [quo/list-item
      {:size                :small
       :title               (i18n/label :t/set-dapp-access-permissions)
       :on-press            #(re-frame/dispatch [:navigate-to :dapps-permissions])
       :accessibility-label :dapps-permissions-button
       :chevron             true}]
     [quo/list-item
      {:size                    :small
       :title                   (if platform/android?
                                  (i18n/label :t/hide-content-when-switching-apps)
                                  (i18n/label :t/hide-content-when-switching-apps-ios))
       :container-margin-bottom 8
       :active                  preview-privacy?
       :accessory               :switch
       :on-press                #(re-frame/dispatch
                                  [:multiaccounts.ui/preview-privacy-mode-switched
                                   ((complement boolean) preview-privacy?)])}]
     (when config/collectibles-enabled?
       [quo/list-item
        {:size                    :small
         :title                   (i18n/label :t/display-collectibles)
         :container-margin-bottom 8
         :active                  opensea-enabled?
         :accessory               :switch
         :on-press                #(re-frame/dispatch
                                    [::multiaccounts.update/toggle-opensea-nfts-visiblity
                                     (not opensea-enabled?)])}])
     [quo/list-item
      {:size                :small
       :title               (i18n/label :t/chat-link-previews)
       :chevron             true
       :on-press            #(re-frame/dispatch [:navigate-to :link-previews-settings])
       :accessibility-label :chat-link-previews}]
     [quo/list-item
      {:size                :small
       :title               (i18n/label :t/accept-new-chats-from)
       :chevron             true
       :accessory           :text
       :accessory-text      (i18n/label (if messages-from-contacts-only
                                          :t/contacts
                                          :t/anyone))
       :on-press            #(re-frame/dispatch [:navigate-to :messages-from-contacts-only])
       :accessibility-label :accept-new-chats-from}]
     (when (not keycard?)
       [quo/list-item
        {:size                :small
         :title               (i18n/label :t/reset-password)
         :chevron             true
         :accessory           :text
         :on-press            #(do
                                 (re-frame/dispatch [::reset-password/clear-form-vals])
                                 (re-frame/dispatch [:navigate-to :reset-password]))
         :accessibility-label :reset-password}])
     (when platform/android?
       [quo/list-item
        {:size               :small
         :title              (i18n/label :t/webview-camera-permission-requests)
         :active             webview-allow-permission-requests?
         :accessory          :switch
         :subtitle           (i18n/label :t/webview-camera-permission-requests-subtitle)
         :subtitle-max-lines 2
         :on-press           #(re-frame/dispatch
                               [:multiaccounts.ui/webview-permission-requests-switched
                                ((complement boolean) webview-allow-permission-requests?)])}])
     (when (not keycard?)
       [quo/list-item
        {:size                :small
         :title               (i18n/label :t/manage-keys-and-storage)
         :chevron             true
         :on-press            #(re-frame/dispatch [::key-storage/logout-and-goto-key-storage])
         :accessibility-label :key-managment}])

     [separator]
     [quo/list-header (i18n/label :t/privacy-photos)]
     [quo/list-item
      {:size                :small
       :title               (i18n/label :t/show-profile-pictures)
       :accessibility-label :show-profile-pictures
       :accessory           :text
       :accessory-text      (get titles profile-pictures-visibility)
       :on-press            #(re-frame/dispatch [:navigate-to :privacy-and-security-profile-pic])
       :chevron             true}]
     [quo/list-item
      {:size                :small
       :title               (i18n/label :t/show-profile-pictures-to)
       :disabled            (not has-picture)
       :accessibility-label :show-profile-pictures-to
       :accessory           :text
       :accessory-text      (get titles profile-pictures-show-to)
       :on-press            #(re-frame/dispatch [:navigate-to :privacy-and-security-profile-pic-show-to])
       :chevron             true}]

     [separator]
     [quo/list-item
      {:size                :small
       :theme               :negative
       :title               (i18n/label :t/delete-my-profile)
       :on-press            #(re-frame/dispatch [:navigate-to :delete-profile])
       :accessibility-label :dapps-permissions-button
       :chevron             true}]]))

(defn ppst-radio-item
  [id value]
  [quo/list-item
   {:active    (= value id)
    :accessory :radio
    :title     (get titles id)
    :on-press  #(re-frame/dispatch [:multiaccounts.ui/profile-picture-show-to-switched id])}])

(views/defview profile-pic-show-to
  []
  (views/letsubs [{:keys [profile-pictures-show-to]} [:multiaccount]]
    [react/view {:margin-top 8}
     [ppst-radio-item constants/profile-pictures-show-to-everyone profile-pictures-show-to]
     [ppst-radio-item constants/profile-pictures-show-to-contacts-only profile-pictures-show-to]
     [ppst-radio-item constants/profile-pictures-show-to-none profile-pictures-show-to]

     [react/view {:style {:margin-horizontal 16}}
      [quo/text {:color :secondary}
       (i18n/label :t/privacy-show-to-warning)]]]))

(defn ppvf-radio-item
  [id value]
  [quo/list-item
   {:active    (= value id)
    :accessory :radio
    :title     (get titles id)
    :on-press  #(re-frame/dispatch [:multiaccounts.ui/appearance-profile-switched id])}])

(views/defview profile-pic
  []
  (views/letsubs [{:keys [profile-pictures-visibility]} [:multiaccount]]
    [react/view {:margin-top 8}
     [ppvf-radio-item constants/profile-pictures-visibility-everyone profile-pictures-visibility]
     [ppvf-radio-item constants/profile-pictures-visibility-contacts-only profile-pictures-visibility]
     [ppvf-radio-item constants/profile-pictures-visibility-none profile-pictures-visibility]]))
