(ns status-im.ui.screens.accounts.recover.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.accounts.recover.styles :as styles]
            [status-im.ui.components.common.styles :as common.styles]
            [status-im.ui.screens.accounts.recover.db :as recover.db]
            [status-im.ui.screens.accounts.db :as db]
            [cljs.spec.alpha :as spec]
            [status-im.ui.components.common.common :as components.common]))

(defview passphrase-input [passphrase]
  (letsubs [error [:get-in [:accounts/recover :passphrase-error]]]
    [text-input/text-input-with-label
     {:style          {:flex 1}
      :height         92
      :label          (i18n/label :t/passphrase)
      :placeholder    (i18n/label :t/enter-12-words)
      :multiline      true
      :default-value  passphrase
      :on-change-text #(re-frame/dispatch [:set-in [:accounts/recover :passphrase] %])
      :error          error}]))

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

(defview ^:theme ^:avoid-keyboard? recover []
  (letsubs [{:keys [passphrase password]} [:get :accounts/recover]]
    (let [valid-form? (and
                        (spec/valid? ::recover.db/passphrase passphrase)
                        (spec/valid? ::db/password password))]
      [react/view common.styles/flex
       [toolbar/toolbar nil toolbar/default-nav-back
        [toolbar/content-title (i18n/label :t/sign-in-to-another)]]
       [components.common/separator]
       [react/view {:margin 16}
        [passphrase-input (or passphrase "")]
        [password-input (or password "")]]
       [react/view common.styles/flex]
       [react/view {:style styles/bottom-button-container}
        [react/view common.styles/flex]
        [components.common/bottom-button
         {:forward?  true
          :label     (i18n/label :t/sign-in)
          :disabled? (not valid-form?)
          :on-press  #(re-frame/dispatch [:recover-account passphrase password])}]]])))
