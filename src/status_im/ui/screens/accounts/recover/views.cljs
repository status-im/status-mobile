(ns status-im.ui.screens.accounts.recover.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]
                    :as views])
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
            [status-im.utils.security :as security]
            [status-im.utils.platform :as platform]
            [clojure.string :as string]))

(defview passphrase-input [passphrase error warning]
  (letsubs [input-ref (reagent/atom nil)]
    [text-input/text-input-with-label
     {:style               styles/recovery-phrase-input
      :height              92
      :ref                 (partial reset! input-ref)
      :label               (i18n/label :t/recovery-phrase)
      :accessibility-label :enter-12-words
      :placeholder         (i18n/label :t/enter-12-words)
      :multiline           true
      :default-value       passphrase
      :auto-correct        false
      :on-change-text      #(re-frame/dispatch [:accounts.recover.ui/passphrase-input-changed (security/mask-data %)])
      :on-blur             #(re-frame/dispatch [:accounts.recover.ui/passphrase-input-blured])
      :error               (cond error (i18n/label error)
                                 warning (i18n/label warning))}]))

(defview password-input [password error on-submit-editing]
  (views/letsubs [inp-ref (atom nil)]
    {:component-will-update
     (fn [_ [_ new-password]]
       (when (and (string? new-password)
                  (string/blank? new-password)
                  @inp-ref)
         (.clear @inp-ref)))}
    [react/view {:style                       styles/password-input
                 :important-for-accessibility :no-hide-descendants}
     [text-input/text-input-with-label
      {:label               (i18n/label :t/password)
       :accessibility-label :enter-password
       :placeholder         (i18n/label :t/enter-password)
       :default-value       password
       :auto-focus          false
       :on-change-text      #(re-frame/dispatch [:accounts.recover.ui/password-input-changed (security/mask-data %)])
       :on-blur             #(re-frame/dispatch [:accounts.recover.ui/password-input-blured])
       :secure-text-entry   true
       :error               (when error (i18n/label error))
       :on-submit-editing   on-submit-editing
       :ref                 #(reset! inp-ref %)}]]))

(defview recover []
  (letsubs [recovered-account [:get-recover-account]
            node-status? [:get :node/status]]
    (let [{:keys [passphrase password passphrase-valid? password-valid?
                  password-error passphrase-error passphrase-warning processing?]} recovered-account
          node-stopped? (or (nil? node-status?)
                            (= :stopped node-status?))
          valid-form? (and password-valid? passphrase-valid?)
          disabled?   (or (not recovered-account)
                          processing?
                          (not valid-form?)
                          (not node-stopped?))
          sign-in     #(re-frame/dispatch [:accounts.recover.ui/sign-in-button-pressed])]
      [react/keyboard-avoiding-view {:style styles/screen-container}
       [status-bar/status-bar]
       [toolbar/toolbar nil toolbar/default-nav-back
        [toolbar/content-title (i18n/label :t/sign-in-to-another)]]
       [react/view styles/inputs-container
        [passphrase-input (or passphrase "") passphrase-error passphrase-warning]
        [password-input (or password "") password-error (when-not disabled? sign-in)]
        (when platform/desktop?
          [react/i18n-text {:style styles/recover-release-warning
                            :key   :recover-account-warning}])]
       [react/view components.styles/flex]
       (if processing?
         [react/view styles/processing-view
          [react/activity-indicator {:animating true}]
          [react/i18n-text {:style styles/sign-you-in
                            :key   :sign-you-in}]]
         [react/view {:style styles/bottom-button-container}
          [react/view {:style components.styles/flex}]
          [components.common/bottom-button
           {:forward?  true
            :label     (i18n/label :t/sign-in)
            :disabled? disabled?
            :on-press  sign-in}]])])))
