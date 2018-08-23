(ns status-im.ui.screens.accounts.recover.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.accounts.recover.styles :as styles]
            [status-im.ui.components.styles :as components.styles]
            [status-im.utils.config :as config]
            [status-im.utils.core :as utils.core]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [status-im.ui.components.common.common :as components.common]
            [status-im.utils.security :as security]))

(defview passphrase-input [passphrase error warning]
  (letsubs [input-ref (reagent/atom nil)]
    {:component-did-mount (fn [_] (when config/testfairy-enabled?
                                    (.hideView js-dependencies/testfairy @input-ref)))}
    [text-input/text-input-with-label
     {:style               styles/recovery-phrase-input
      :height              92
      :ref                 (partial reset! input-ref)
      :label               (i18n/label :t/recovery-phrase)
      :placeholder         (i18n/label :t/enter-12-words)
      :multiline           true
      :default-value       passphrase
      :auto-correct        false
      :on-change-text      #(re-frame/dispatch [:recover/set-phrase (security/mask-data %)])
      :on-blur             #(re-frame/dispatch [:recover/validate-phrase])
      :error               (cond error (i18n/label error)
                                 warning (i18n/label warning))}]))

(defview password-input [password error]
  [react/view {:style                       styles/password-input
               :important-for-accessibility :no-hide-descendants}
   [text-input/text-input-with-label
    {:label             (i18n/label :t/password)
     :placeholder       (i18n/label :t/enter-password)
     :default-value     password
     :auto-focus        false
     :on-change-text    #(re-frame/dispatch [:recover/set-password (security/mask-data %)])
     :on-blur           #(re-frame/dispatch [:recover/validate-password])
     :secure-text-entry true
     :error             (when error (i18n/label error))}]])

(defview recover []
  (letsubs [{:keys [passphrase password processing passphrase-valid? password-valid?
                    password-error passphrase-error passphrase-warning processing?]} [:get-recover-account]]
    (let [valid-form? (and password-valid? passphrase-valid?)]
      [react/keyboard-avoiding-view {:style styles/screen-container}
       [status-bar/status-bar]
       [toolbar/toolbar nil toolbar/default-nav-back
        [toolbar/content-title (i18n/label :t/sign-in-to-another)]]
       [components.common/separator]
       [react/view styles/inputs-container
        [passphrase-input (or passphrase "") passphrase-error passphrase-warning]
        [password-input (or password "") password-error]]
       [react/view components.styles/flex]
       (if processing
         [react/view styles/processing-view
          [react/activity-indicator {:animating true}]
          [react/i18n-text {:style styles/sign-you-in
                            :key   :sign-you-in}]]
         [react/view {:style styles/bottom-button-container}
          [react/view {:style components.styles/flex}]
          [components.common/bottom-button
           {:forward?  true
            :label     (i18n/label :t/sign-in)
            :disabled? (or processing? (not valid-form?))
            :on-press  (utils.core/wrap-call-once!
                        #(re-frame/dispatch [:recover-account-with-checks]))}]])])))
