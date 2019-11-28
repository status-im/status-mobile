(ns status-im.ui.screens.privacy-and-security-settings.views
  (:require [clojure.string :as string]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.multiaccounts.biometric.core :as biometric])
  (:require-macros [status-im.utils.views :as views]))

(defn- list-data [show-backup-seed? settings supported-biometric-auth biometric-auth? keycard?]
  [{:type                 :section-header
    :title                :t/security
    :container-margin-top 6}
   {:type                    :small
    :title                   :t/back-up-seed-phrase
    :accessibility-label     :back-up-recovery-phrase-button
    :disabled?               (not show-backup-seed?)
    ;; TODO - remove container bottom margin
    ;; when items below are implemented
    :container-margin-bottom 8
    :on-press
    #(re-frame/dispatch [:navigate-to :backup-seed])
    :accessories
    (when show-backup-seed? [[components.common/counter {:size 22} 1]
                             :chevron])}
   {:type                    :small
    :title                   (str (i18n/label :t/lock-app-with) " " (biometric/get-label supported-biometric-auth))
    :container-margin-bottom 8
    :accessibility-label     :biometric-auth-settings-switch
    :disabled?               (not (some? supported-biometric-auth))
    :accessories             [[react/switch
                               {:track-color     #js {:true colors/blue :false nil}
                                :value           (boolean biometric-auth?)
                                :on-value-change #(re-frame/dispatch [:multiaccounts.ui/biometric-auth-switched %])
                                :disabled        (not supported-biometric-auth)}]]
    :on-press                #(re-frame/dispatch [:multiaccounts.ui/biometric-auth-switched
                                                  ((complement boolean) biometric-auth?)])}
   ;; TODO - uncomment when implemented
   ;; {:type        :small
   ;;  :title       :t/change-password
   ;;  :accessories [:chevron]}
   ;; {:type                    :small
   ;;  :title                   :t/change-passcode
   ;;  :accessories             [:chevron]
   ;;  :container-margin-bottom 8}
   {:type :divider}
   {:container-margin-top 8
    :type                 :section-header
    :title                :t/privacy}
   {:type                :small
    :title               :t/set-dapp-access-permissions
    :on-press            #(re-frame/dispatch [:navigate-to :dapps-permissions])
    :accessibility-label :dapps-permissions-button
    :accessories         [:chevron]}
   {:type                    :small
    :title                   :t/hide-content-when-switching-apps
    :container-margin-bottom 8
    :accessories
    [[react/switch
      {:track-color #js {:true colors/blue :false nil}
       :value       (boolean (:preview-privacy? settings))
       :on-value-change
       #(re-frame/dispatch
         [:multiaccounts.ui/preview-privacy-mode-switched %])
       :disabled    false}]]
    :on-press
    #(re-frame/dispatch
      [:multiaccounts.ui/preview-privacy-mode-switched
       ((complement boolean) (:preview-privacy? settings))])}
   {:type :divider}
   ;; TODO - uncomment when implemented
   (comment
     {:container-margin-top    8
      :type                    :small
      :title                   :t/delete-my-account
      :container-margin-bottom 24
      :theme                   :action-destructive})])

(views/defview privacy-and-security []
  (views/letsubs [{:keys [seed-backed-up? mnemonic]} [:multiaccount]
                  settings                 [:multiaccount-settings]
                  supported-biometric-auth [:supported-biometric-auth]
                  auth-method              [:auth-method]
                  keycard-multiaccount?    [:keycard-multiaccount?]]
    (let [show-backup-seed? (and (not seed-backed-up?)
                                 (not (string/blank? mnemonic)))]
      [react/view {:flex 1 :background-color colors/white}
       [toolbar/simple-toolbar
        (i18n/label :t/privacy-and-security)]
       [list/flat-list
        {:data      (list-data show-backup-seed? settings supported-biometric-auth
                               (= auth-method "biometric") keycard-multiaccount?)
         :key-fn    (fn [_ i] (str i))
         :render-fn list/flat-list-generic-render-fn}]])))
