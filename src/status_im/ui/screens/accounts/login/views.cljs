(ns status-im.ui.screens.accounts.login.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [status-im.ui.screens.accounts.styles :as ast]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as act]
            [status-im.ui.screens.accounts.login.styles :as styles]
            [status-im.ui.components.react :as react]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as components]
            [status-im.ui.components.common.common :as components.common]
            [status-im.chat.views.photos :as photos]
            [re-frame.core :as re-frame]
            [cljs.spec.alpha :as spec]
            [status-im.ui.screens.accounts.db :as db]))

(defn login-toolbar [can-navigate-back?]
  [toolbar/toolbar
   nil
   (when can-navigate-back?
     [toolbar/nav-button act/default-back])
   [toolbar/content-title (i18n/label :t/sign-in-to-status)]])

(defn login-account [password-text-input address password]
  (.blur password-text-input)
  (re-frame/dispatch [:login-account address password]))

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

(defn account-login-badge [photo-path name]
  [react/view styles/login-badge
   [photos/photo photo-path {:size styles/login-badge-image-size}]
   [react/view
    [react/text {:style         styles/login-badge-name
                 :numberOfLines 1}
     name]]])

(defview login []
  (letsubs [{:keys [address photo-path name password error processing]} [:get :accounts/login]
            can-navigate-back? [:can-navigate-back?]
            password-text-input (atom nil)]
    [react/keyboard-avoiding-view {:style ast/accounts-view}
     [status-bar/status-bar]
     [login-toolbar can-navigate-back?]
     [components.common/separator]
     [react/view styles/login-view
      [react/view styles/login-badge-container
       [account-login-badge photo-path name]
       [react/view styles/password-container
        [text-input/text-input-with-label
         {:label             (i18n/label :t/password)
          :placeholder       (i18n/label :t/password)
          :ref               #(reset! password-text-input %)
          :auto-focus        can-navigate-back? ;;this needed because keyboard overlays testfairy alert
          :on-submit-editing #(login-account @password-text-input address password)
          :on-change-text    #(do
                                (re-frame/dispatch [:set-in [:accounts/login :password] %])
                                (re-frame/dispatch [:set-in [:accounts/login :error] ""]))
          :secure-text-entry true
          :style             {:height 52}
          :error             (when (pos? (count error)) (i18n/label (error-key error)))}]]]]
     [react/view styles/processing-view
      (when processing
        [components/activity-indicator {:animating true}])
      (when processing
        [react/text {:style styles/sign-you-in}
         (i18n/label :t/sign-you-in)])]
     (when-not processing
       [react/view {:style styles/bottom-button-container}
        (when-not can-navigate-back?
          [components.common/bottom-button
           {:label    (i18n/label :t/other-accounts)
            :on-press #(re-frame/dispatch [:navigate-to-clean :accounts])}])
        [react/view {:style {:flex 1}}]
        [components.common/bottom-button
         {:forward?  true
          :label     (i18n/label :t/sign-in)
          :disabled? (not (spec/valid? ::db/password password))
          :on-press  #(login-account @password-text-input address password)}]])]))
