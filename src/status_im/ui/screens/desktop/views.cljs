(ns status-im.ui.screens.desktop.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.screens.desktop.main.views :as main.views]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.intro.views :as intro.views]
            [status-im.ui.screens.accounts.create.views :as create.views]
            [status-im.ui.screens.accounts.login.views :as login.views]
            [status-im.ui.screens.accounts.recover.views :as recover.views]
            [status-im.ui.screens.accounts.views :as accounts.views]))

(enable-console-print!)

(views/defview main []
  (views/letsubs [view-id [:get :view-id]]
    (let [component (case view-id
                      :intro intro.views/intro
                      :accounts accounts.views/accounts
                      :recover recover.views/recover
                      :create-account create.views/create-account
                      (:new-contact
                       :advanced-settings
                       :chat :home
                       :qr-code
                       :chat-profile) main.views/main-views
                      :login login.views/login
                      react/view)]
      [react/view {:style {:flex 1}}
       [component]])))
