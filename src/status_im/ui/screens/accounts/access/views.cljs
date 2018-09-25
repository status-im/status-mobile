(ns status-im.ui.screens.accounts.access.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.accounts.access.styles :as styles]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [status-im.i18n :as i18n]
            [status-im.utils.config :as config]
            [status-im.utils.security :as security]
            [status-im.ui.components.toolbar.actions :as actions]))

(def steps
  {:passphrase       nil
   :enter-password   {:input-key           :password
                      :input-label         (i18n/label :t/password)
                      :input-placeholder   (i18n/label :t/password-placeholder)
                      :input-description   (i18n/label :t/password-description)
                      :accessibility-label :password}
   :confirm-password {:input-key           :password-confirm
                      :input-label         (i18n/label :t/confirm)
                      :input-placeholder   (i18n/label :t/password-placeholder2)
                      :input-description   (i18n/label :t/password-description)
                      :accessibility-label :password-confirmation}})

(defview passphrase-input [passphrase error warning]
  (letsubs [input-ref (reagent/atom nil)]
    {:component-did-mount (fn [_] (when config/testfairy-enabled?
                                    (.hideView js-dependencies/testfairy @input-ref)))}
    [text-input/text-input-with-label
     {:style               styles/recovery-phrase-input
      :accessibility-label :recovery-phrase
      :height              92
      :ref                 (partial reset! input-ref)
      :label               (i18n/label :t/recovery-phrase)
      :placeholder         (i18n/label :t/enter-12-words)
      :multiline           true
      :default-value       passphrase
      :auto-correct        false
      :on-change-text      #(re-frame/dispatch [:accounts.access.ui/passphrase-input-changed (security/mask-data %)])
      :on-blur             #(re-frame/dispatch [:accounts.access.ui/passphrase-input-blured])
      :error               (cond error (i18n/label error)
                                 warning (i18n/label warning))}]))

(defview input [step error]
  (let [{:keys [input-label
                input-placeholder
                input-key
                accessibility-label]} (get steps step)]
    [text-input/text-input-with-label
     {:label               input-label
      :accessibility-label accessibility-label
      :placeholder         input-placeholder
      :on-change-text      (fn [text]
                             (let [text (security/mask-data text)]
                               (re-frame/dispatch [:accounts.access.ui/input-text-changed input-key text])))
      :secure-text-entry   (boolean (#{:enter-password :confirm-password} step))
      :error               error}]))

(defview access-account []
  (letsubs [step [:get-in [:accounts/access :step]]
            next-enabled? [:get-account-access-next-enabled?]
            accessed-account [:get-access-account]]
    (let [{:keys [processing passphrase passphrase-error passphrase-warning error]} accessed-account]
      ^{:key (str "step" step)}
      [react/keyboard-avoiding-view {:style styles/screen-container}
       [status-bar/status-bar]
       [toolbar/toolbar nil (toolbar/nav-button
                             (actions/back #(re-frame/dispatch [:accounts.access.ui/step-back-pressed step])))
        [toolbar/content-title (i18n/label :t/access-account)]]
       [components.common/separator]
       [react/view styles/inputs-container
        (if (= :passphrase step)
          [passphrase-input (or passphrase "") passphrase-error passphrase-warning]
          [react/view {:important-for-accessibility :no-hide-descendants}
           [input step error]
           [react/text {:style styles/input-description}
            (get-in steps [step :input-description])]])]
       [react/view components.styles/flex]
       (if processing
         [react/view styles/processing-view
          [react/activity-indicator {:animating true}]
          [react/i18n-text {:style styles/sign-you-in
                            :key   :sign-you-in}]]
         [react/view {:style styles/bottom-button-container}
          [react/view {:style components.styles/flex}]
          [components.common/bottom-button
           {:forward?            true
            :label               (when (= step :confirm-password)
                                   (i18n/label :t/access))
            :accessibility-label (if (= step :confirm-password)
                                   :access-button
                                   :next-button)
            :disabled?           (not next-enabled?)
            :on-press            #(re-frame/dispatch [:accounts.access.ui/next-step-pressed step])}]])])))