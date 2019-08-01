(ns status-im.ui.screens.multiaccounts.login.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.multiaccounts.styles :as ast]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.checkbox.view :as checkbox]
            [status-im.ui.components.toolbar.actions :as act]
            [status-im.ui.screens.multiaccounts.login.styles :as styles]
            [status-im.ui.components.react :as react]
            [status-im.i18n :as i18n]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.react :as components]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.screens.chat.photos :as photos]
            [re-frame.core :as re-frame]
            [status-im.utils.platform :as platform]
            [status-im.utils.security :as security]))

(defn login-toolbar [can-navigate-back?]
  [toolbar/toolbar
   {:style {:border-bottom-width 0
            :margin-top          0}}
   (when can-navigate-back?
     [toolbar/nav-button (act/back #(re-frame/dispatch [:navigate-to-clean :multiaccounts]))])
   nil])

(defn login-multiaccount [password-text-input]
  (.blur password-text-input)
  (re-frame/dispatch [:multiaccounts.login.ui/password-input-submitted]))

(defn- error-key [error]
  ;; TODO Improve selection logic when status-go provide an error code
  ;; see https://github.com/status-im/status-go/issues/278
  (cond
    (string/starts-with? error "there is no running node")
    :t/node-unavailable

    (or
     (string/starts-with? error "cannot retrieve a valid key")
     (string/starts-with? error "could not decrypt key"))
    :t/wrong-password

    :else
    :t/unknown-status-go-error))

(defn multiaccount-login-badge [photo-path name public-key]
  [react/view styles/login-badge
   [photos/photo photo-path {:size styles/login-badge-image-size}]
   [react/view
    [react/text {:style          styles/login-badge-name
                 :ellipsize-mode :middle
                 :numberOfLines  1}
     name]
    [react/text {:style styles/login-badge-pubkey}
     (utils/get-shortened-address public-key)]]])

(defview login []
  (letsubs [{:keys [photo-path name public-key error processing save-password? can-save-password?] :as multiaccount} [:multiaccounts/login]
            can-navigate-back? [:can-navigate-back?]
            password-text-input (atom nil)
            sign-in-enabled? [:sign-in-enabled?]
            view-id [:view-id]]
    [react/keyboard-avoiding-view {:style ast/multiaccounts-view}
     [status-bar/status-bar]
     [login-toolbar can-navigate-back?]
     [react/scroll-view styles/login-view
      [react/view styles/login-badge-container
       [multiaccount-login-badge photo-path name public-key]
       [react/view {:style                       styles/password-container
                    :important-for-accessibility :no-hide-descendants}
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
          :secure-text-entry true
          :error             (when (not-empty error) (i18n/label (error-key error)))}]]

       (when-not platform/desktop?
         ;; saving passwords is unavailable on Desktop
         (if (and platform/android? (not can-save-password?))
           ;; on Android, there is much more reasons for the password save to be unavailable,
           ;; so we don't show the checkbox whatsoever but put a label explaining why it happenned.
           [react/i18n-text {:style styles/save-password-unavailable-android
                             :key :save-password-unavailable-android}]
           [react/view {:style {:flex-direction  :row
                                :align-items     :center
                                :justify-content :flex-start}}
            [checkbox/checkbox {:checked? save-password?
                                :style {:padding-left 0 :padding-right 10}
                                :on-value-change #(re-frame/dispatch [:set-in [:multiaccounts/login :save-password?] %])}]
            [react/text (i18n/label :t/save-password)]]))]]
     (when processing
       [react/view styles/processing-view
        [components/activity-indicator {:animating true}]
        [react/i18n-text {:style styles/processing :key :processing}]])
     [react/view {:style styles/bottom-button-container}
      [components.common/button
       {:label        (i18n/label :t/access-key)
        :button-style styles/bottom-button
        :background?  false
        :on-press     #(do
                         (react/dismiss-keyboard!)
                         (re-frame/dispatch [:multiaccounts.recover.ui/recover-multiaccount-button-pressed]))}]
      [components.common/button
       {:label     (i18n/label :t/submit)
        :button-style styles/bottom-button
        :label-style {:color (if (or (not sign-in-enabled?) processing) colors/gray colors/blue)}
        :background? true
        :disabled? (or (not sign-in-enabled?) processing)
        :on-press  #(login-multiaccount @password-text-input)}]]]))
