(ns status-im.ui.screens.accounts.login.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch dispatch-sync]]
            [status-im.ui.screens.accounts.styles :as ast]
            [status-im.ui.screens.accounts.views :refer [account-badge]]
            [status-im.components.text-input-with-label.view :refer [text-input-with-label]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar-new.view :refer [toolbar]]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.ui.screens.accounts.login.styles :as st]
            [status-im.components.react :refer [view
                                                text
                                                touchable-highlight]]
            [status-im.i18n :as i18n]
            [status-im.components.react :as components]))

(defn login-toolbar []
  [toolbar {:background-color :transparent
            :hide-border?     true
            :title-style      {:color :white}
            :nav-action       (act/back-white #(dispatch [:navigate-back]))
            :title            (i18n/label :t/sign-in-to-status)}])

(def password-text-input (atom nil))

(defn login-account [password-text-input address password]
  (.blur @password-text-input)
  (dispatch [:login-account address password]))

(defview login []
  (letsubs [{:keys [address photo-path name password error processing]} [:get :accounts/login]]
    [view ast/accounts-container
     [status-bar {:type :transparent}]
     [login-toolbar]
     [view st/login-view
      [view st/login-badge-container
       [account-badge address photo-path name]
       [view {:height 8}]
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
                               :error             (when (pos? (count error)) (i18n/label :t/wrong-password))}]]
      [view {:margin-top 16}
       [touchable-highlight {:on-press #(login-account password-text-input address password)}
        [view st/sign-in-button
         [text {:style st/sign-it-text} (i18n/label :t/sign-in)]]]]]
     (when processing
       [view st/processing-view
        [components/activity-indicator {:animating true}]])]))
