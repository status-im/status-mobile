(ns status-im.ui.screens.desktop.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.screens.desktop.main.views :as main.views]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.intro.views :as intro.views]
            [status-im.ui.screens.group.views :refer [contact-toggle-list
                                                      new-group
                                                      add-participants-toggle-list]]
            [status-im.ui.screens.profile.group-chat.views :refer [group-chat-profile]]
            [status-im.ui.screens.accounts.create.views :as create.views]
            [status-im.ui.screens.accounts.login.views :as login.views]
            [status-im.ui.screens.accounts.recover.views :as recover.views]
            [status-im.ui.screens.accounts.views :as accounts.views]
            [status-im.utils.platform :as platform]
            [status-im.i18n :as i18n]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [taoensso.timbre :as log]
            [status-im.utils.utils :as utils]))

(enable-console-print!)

(views/defview main []
  (views/letsubs [view-id [:get :view-id]
                  version [:get-app-version]]
    {:component-did-mount
     (fn []
       (.getValue rn-dependencies/desktop-config "desktop-alpha-warning-shown-for-version"
                  #(when-not (= %1 version)
                     (.setValue rn-dependencies/desktop-config "desktop-alpha-warning-shown-for-version" version)
                     (utils/show-popup nil (i18n/label :desktop-alpha-release-warning)))))}

    (let [component (case view-id
                      :intro intro.views/intro
                      :accounts accounts.views/accounts
                      :recover recover.views/recover
                      :create-account create.views/create-account
                      :new-group  new-group
                      :contact-toggle-list contact-toggle-list
                      :group-chat-profile group-chat-profile
                      :add-participants-toggle-list add-participants-toggle-list

                      (:desktop/new-one-to-one
                       :desktop/new-group-chat
                       :desktop/new-public-chat
                       :advanced-settings
                       :help-center
                       :installations
                       :chat
                       :home
                       :qr-code
                       :chat-profile
                       :backup-recovery-phrase) main.views/main-views
                      :login login.views/login
                      react/view)]
      [react/view {:style {:flex 1}}
       [component]
       [main.views/popup-view]])))
