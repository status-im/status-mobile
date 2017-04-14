(ns status-im.accounts.login.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [dispatch dispatch-sync]]
            [status-im.accounts.styles :as ast]
            [status-im.accounts.screen :refer [account-bage]]
            [status-im.components.text-input-with-label.view :refer [text-input-with-label]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar-new.view :refer [toolbar]]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.accounts.login.styles :as st]
            [status-im.components.react :refer [view
                                                text
                                                touchable-highlight]]
            [status-im.i18n :as i18n]))

(defn login-toolbar []
  [toolbar {:background-color :transparent
            :hide-border?     true
            :title-style      {:color :white}
            :nav-action       (act/back-white #(dispatch [:navigate-back]))
            :actions          [{:image :blank}]
            :title            (i18n/label :t/sign-in-to-status)}])

(defview login []
  [{:keys [address photo-path name password error]} [:get :login]]
  [view ast/accounts-container
   [status-bar {:type :transparent}]
   [login-toolbar]
   [view st/login-view
    [view st/login-badge-container
     [account-bage address photo-path name]
     [view {:height 8}]
     [text-input-with-label {:label             (i18n/label :t/password)
                             :auto-capitalize   :none
                             :hide-underline?   true
                             :on-change-text    #(do
                                                   (dispatch [:set-in [:login :password] %])
                                                   (dispatch [:set-in [:login :error] ""]))
                             :auto-focus        true
                             :secure-text-entry true
                             :error             (when (pos? (count error)) (i18n/label :t/wrong-password))}]]
    [view {:margin-top 16}
     [touchable-highlight {:on-press #(dispatch [:login-account address password])}
      [view st/sign-in-button
       [text {:style st/sign-it-text} (i18n/label :t/sign-in)]]]]]])
