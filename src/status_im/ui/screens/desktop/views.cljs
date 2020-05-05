(ns status-im.ui.screens.desktop.views
  (:require #_[status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.desktop.main.views :as main.views]
            [status-im.ui.screens.group.views
             :refer
             [add-participants-toggle-list contact-toggle-list new-group]]
            [status-im.ui.screens.intro.views :as intro.views]
            [status-im.ui.screens.multiaccounts.login.views :as login.views]
            [status-im.ui.screens.multiaccounts.views :as multiaccounts.views]
            [status-im.ui.screens.profile.group-chat.views
             :refer
             [group-chat-profile]]
            #_[status-im.utils.utils :as utils])
  (:require-macros [status-im.utils.views :as views]))

(enable-console-print!)

(views/defview main []
  (views/letsubs [view-id [:view-id]
                  #_#_version [:get-app-version]]
    {:component-did-mount
     (fn []
       #_(.getValue rn-dependencies/desktop-config "desktop-alpha-warning-shown-for-version"
                    #(when-not (= %1 version)
                       (.setValue ^js rn-dependencies/desktop-config "desktop-alpha-warning-shown-for-version" version)
                       (utils/show-popup nil (i18n/label :desktop-alpha-release-warning)))))}

    (let [component (case view-id
                      :intro intro.views/intro
                      :multiaccounts multiaccounts.views/multiaccounts
                      :new-group  new-group
                      :contact-toggle-list contact-toggle-list
                      :group-chat-profile group-chat-profile
                      :add-participants-toggle-list add-participants-toggle-list

                      (:desktop/new-one-to-one
                       :desktop/new-group-chat
                       :desktop/new-public-chat
                       :advanced-settings
                       :edit-mailserver
                       :bootnodes-settings
                       :edit-bootnode
                       :about-app
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
