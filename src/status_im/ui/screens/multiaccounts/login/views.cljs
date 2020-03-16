(ns status-im.ui.screens.multiaccounts.login.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.checkbox.view :as checkbox]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.screens.multiaccounts.login.styles :as styles]
            [status-im.ui.screens.multiaccounts.styles :as ast]
            [status-im.utils.platform :as platform]
            [status-im.utils.security :as security]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.topbar :as topbar])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn login-multiaccount [password-text-input]
  (.blur password-text-input)
  (re-frame/dispatch [:multiaccounts.login.ui/password-input-submitted]))

(defn multiaccount-login-badge [{:keys [public-key name] :as multiaccount}]
  [react/view styles/login-badge
   [photos/photo
    ;;TODO this should be done in a subscription
    (multiaccounts/displayed-photo multiaccount)
    {:size styles/login-badge-image-size}]
   [react/view
    [react/text {:style          styles/login-badge-name
                 :ellipsize-mode :middle
                 :numberOfLines  1}
     ;;TODO this should be done in a subscription
     name]
    [react/text {:style styles/login-badge-pubkey}
     (utils/get-shortened-address public-key)]]])

(defview login []
  (letsubs [{:keys [error processing save-password?] :as multiaccount} [:multiaccounts/login]
            password-text-input (atom nil)
            sign-in-enabled? [:sign-in-enabled?]
            auth-method [:auth-method]
            view-id [:view-id]
            supported-biometric-auth [:supported-biometric-auth]]
    [react/keyboard-avoiding-view {:style ast/multiaccounts-view}
     [topbar/topbar {}]
     [react/scroll-view {:keyboardShouldPersistTaps :always
                         :style                     styles/login-view}
      [react/view styles/login-badge-container
       [multiaccount-login-badge multiaccount]
       [react/view {:style                       styles/password-container
                    :important-for-accessibility :no-hide-descendants}
        [react/view {:flex-direction :row :align-items :center}
         [react/view {:flex 1}
          [text-input/text-input-with-label
           {:placeholder         (i18n/label :t/enter-your-password)
            :ref                 #(reset! password-text-input %)
            :auto-focus          (= view-id :login)
            :accessibility-label :password-input
            :on-submit-editing   (when sign-in-enabled?
                                   #(login-multiaccount @password-text-input))
            :on-change-text      #(do
                                    (re-frame/dispatch [:set-in [:multiaccounts/login :password]
                                                        (security/mask-data %)])
                                    (re-frame/dispatch [:set-in [:multiaccounts/login :error] ""]))
            :secure-text-entry   true
            :error               (when (not-empty error) error)}]]
         (when (and supported-biometric-auth (= auth-method "biometric"))
           [react/touchable-highlight {:on-press #(re-frame/dispatch [:biometric-authenticate])}
            [react/view {:style styles/biometric-button}
             [icons/icon (if (= supported-biometric-auth :FaceID) :faceid :print)]]])]]
       (when-not platform/desktop?
         ;; saving passwords is unavailable on Desktop
         (if (and platform/android? (not auth-method))
           ;; on Android, there is much more reasons for the password save to be unavailable,
           ;; so we don't show the checkbox whatsoever but put a label explaining why it happenned.
           [react/i18n-text {:style styles/save-password-unavailable-android
                             :key   :save-password-unavailable-android}]
           [react/view {:style {:flex-direction  :row
                                :align-items     :center
                                :justify-content :flex-start
                                :margin-top      19}}
            [checkbox/checkbox {:checked?        save-password?
                                :style           {:margin-left 3 :margin-right 10}
                                :on-value-change #(re-frame/dispatch [:multiaccounts/save-password %])}]
            [react/text (i18n/label :t/save-password)]]))]]
     (when processing
       [react/view styles/processing-view
        [react/activity-indicator {:animating true}]
        [react/i18n-text {:style styles/processing :key :processing}]])
     [react/view {:style (styles/bottom-button-container)}
      [components.common/button
       {:label        (i18n/label :t/access-key)
        :button-style styles/bottom-button
        :background?  false
        :on-press     #(do
                         (react/dismiss-keyboard!)
                         (re-frame/dispatch [:multiaccounts.recover.ui/recover-multiaccount-button-pressed]))}]
      [components.common/button
       {:label        (i18n/label :t/submit)
        :button-style styles/bottom-button
        :label-style  {:color (if (or (not sign-in-enabled?) processing) colors/gray colors/blue)}
        :background?  true
        :disabled?    (or (not sign-in-enabled?) processing)
        :on-press     #(login-multiaccount @password-text-input)}]]]))
