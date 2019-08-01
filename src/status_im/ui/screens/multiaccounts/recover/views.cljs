(ns status-im.ui.screens.multiaccounts.recover.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]
                    :as views])
  (:require [reagent.core :as reagent]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.multiaccounts.recover.styles :as styles]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.common.common :as components.common]
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
       :secure-text-entry   true
       :error               (when error (i18n/label error))
       :on-submit-editing   on-submit-editing
       :ref                 #(reset! inp-ref %)}]]))

(defview recover []
  [react/keyboard-avoiding-view {:style styles/screen-container}
   [status-bar/status-bar]
   [toolbar/toolbar nil toolbar/default-nav-back
    [toolbar/content-title (i18n/label :t/sign-in-to-another)]]
   [react/view styles/inputs-container
    [passphrase-input "" "" ""]
    [password-input "" "" #()]
    (when platform/desktop?
      [react/i18n-text {:style styles/recover-release-warning
                        :key   :recover-multiaccount-warning}])]
   [react/view components.styles/flex]
   [react/view {:style styles/bottom-button-container}
    [react/view {:style components.styles/flex}]]])
