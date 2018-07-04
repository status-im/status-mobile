(ns status-im.ui.screens.accounts.recover.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.accounts.recover.styles :as styles]
            [status-im.ui.screens.accounts.db :as db]
            [status-im.utils.config :as config]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [cljs.spec.alpha :as spec]
            [status-im.ui.components.common.common :as components.common]
            [status-im.utils.security :as security]))

(defview passphrase-input [passphrase]
  (letsubs [error [:get-in [:accounts/recover :passphrase-error]]
            input-ref (reagent/atom nil)]
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
      :on-change-text      #(re-frame/dispatch [:set-in [:accounts/recover :passphrase] (string/lower-case %)])
      :error               error}]))

(defview password-input [password]
  (letsubs [error [:get-in [:accounts/recover :password-error]]]
    [react/view {:margin-top 10}
     [text-input/text-input-with-label
      {:label             (i18n/label :t/password)
       :placeholder       (i18n/label :t/enter-password)
       :default-value     password
       :auto-focus        false
       :on-change-text    #(re-frame/dispatch [:set-in [:accounts/recover :password] %])
       :secure-text-entry true
       :error             error}]]))

(defview recover []
  (letsubs [{:keys [passphrase password processing]} [:get :accounts/recover]]
    (let [words       (ethereum/passphrase->words passphrase)
          valid-form? (and
                       (ethereum/valid-words? words)
                       (spec/valid? ::db/password password))]
      [react/keyboard-avoiding-view {:style styles/screen-container}
       [status-bar/status-bar]
       [toolbar/toolbar nil toolbar/default-nav-back
        [toolbar/content-title (i18n/label :t/sign-in-to-another)]]
       [components.common/separator]
       [react/view {:margin 16}
        [passphrase-input (or passphrase "")]
        [password-input (or password "")]]
       [react/view {:flex 1}]
       (if processing
         [react/view styles/processing-view
          [react/activity-indicator {:animating true}]
          [react/text {:style styles/sign-you-in}
           (i18n/label :t/sign-you-in)]]
         [react/view {:style styles/bottom-button-container}
          [react/view {:style {:flex 1}}]
          [components.common/bottom-button
           {:forward?  true
            :label     (i18n/label :t/sign-in)
            :disabled? (not valid-form?)
            :on-press  (fn [_]
                         (let [masked-passphrase (security/mask-data (ethereum/words->passphrase words))]
                           (re-frame/dispatch [:recover-account masked-passphrase password])))}]])])))
