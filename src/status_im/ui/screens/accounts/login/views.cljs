(ns status-im.ui.screens.accounts.login.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [status-im.ui.screens.accounts.styles :as ast]
            [status-im.ui.screens.accounts.views :refer [account-badge]]
            [status-im.ui.components.text-input-with-label.view :refer [text-input-with-label]]
            [status-im.ui.components.status-bar.view :refer [status-bar]]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as act]
            [status-im.ui.screens.accounts.login.styles :as st]
            [status-im.ui.components.react :as react]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as components]))

(defn login-toolbar []
  [toolbar/toolbar {:background-color :transparent}
   [toolbar/nav-button (act/back-white #(dispatch [:navigate-back]))]
   [toolbar/content-title {:color :white} (i18n/label :t/sign-in-to-status)]])

(def password-text-input (atom nil))

(defn login-account [password-text-input address password]
  (.blur @password-text-input)
  (dispatch [:login-account address password]))

(defn- error-key [error]
  ;; TODO Improve selection logic when status-go provide an error code
  ;; see https://github.com/status-im/status-go/issues/278
  (cond
    (string/starts-with? error "there is no running node")
    :t/node-unavailable

    (string/starts-with? error "cannot retrieve a valid key")
    :t/wrong-password

    :else
    :t/unknown-status-go-error))

(defview login []
  (letsubs [{:keys [address photo-path name password error processing]} [:get :accounts/login]]
    [react/view ast/accounts-container
     [status-bar {:type :transparent}]
     [login-toolbar]
     [react/view st/login-view
      [react/view st/login-badge-container
       [account-badge address photo-path name]
       [react/view {:height 8}]
       [text-input-with-label {:ref               #(reset! password-text-input %)
                               :label             (i18n/label :t/password)
                               :auto-capitalize   :none
                               :hide-underline?   true
                               :on-change-text    #(do
                                                     (dispatch [:set-in [:accounts/login :password] %])
                                                     (dispatch [:set-in [:accounts/login :error] ""]))
                               :on-submit-editing #(login-account password-text-input address password)
                               :auto-focus        true
                               :secure-text-entry true
                               :error             (when (pos? (count error)) (i18n/label (error-key error)))}]]
      (let [enabled? (pos? (count password))]
        [react/view {:margin-top 16}
         [react/touchable-highlight (if enabled? {:on-press #(login-account password-text-input address password)})
          [react/view st/sign-in-button
           [react/text {:style (if enabled? st/sign-it-text st/sign-it-disabled-text)} (i18n/label :t/sign-in)]]]])]
     (when processing
       [react/view st/processing-view
        [components/activity-indicator {:animating true}]])]))
