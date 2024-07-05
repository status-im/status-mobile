(ns legacy.status-im.ui.screens.screens
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.screens.about-app.views :as about-app]
    [legacy.status-im.ui.screens.advanced-settings.views :as advanced-settings]
    [legacy.status-im.ui.screens.appearance.views :as appearance]
    [legacy.status-im.ui.screens.backup-settings.view :as backup-settings]
    [legacy.status-im.ui.screens.bug-report :as bug-report]
    [legacy.status-im.ui.screens.communities.invite :as communities.invite]
    [legacy.status-im.ui.screens.communities.members :as members]
    [legacy.status-im.ui.screens.dapps-permissions.views :as dapps-permissions]
    [legacy.status-im.ui.screens.default-sync-period-settings.view :as default-sync-period-settings]
    [legacy.status-im.ui.screens.fleet-settings.views :as fleet-settings]
    [legacy.status-im.ui.screens.glossary.view :as glossary]
    [legacy.status-im.ui.screens.help-center.views :as help-center]
    [legacy.status-im.ui.screens.link-previews-settings.views :as link-previews-settings]
    [legacy.status-im.ui.screens.log-level-settings.views :as log-level-settings]
    [legacy.status-im.ui.screens.mobile-network-settings.view :as mobile-network-settings]
    [legacy.status-im.ui.screens.notifications-settings.views :as notifications-settings]
    [legacy.status-im.ui.screens.offline-messaging-settings.edit-mailserver.views :as edit-mailserver]
    [legacy.status-im.ui.screens.offline-messaging-settings.views :as offline-messaging-settings]
    [legacy.status-im.ui.screens.pairing.views :as pairing]
    [legacy.status-im.ui.screens.peers-stats :as peers-stats]
    [legacy.status-im.ui.screens.privacy-and-security-settings.delete-profile :as delete-profile]
    [legacy.status-im.ui.screens.privacy-and-security-settings.messages-from-contacts-only :as
     messages-from-contacts-only]
    [legacy.status-im.ui.screens.privacy-and-security-settings.views :as privacy-and-security]
    [legacy.status-im.ui.screens.profile.seed.views :as profile.seed]
    [legacy.status-im.ui.screens.profile.user.views :as profile.user]
    [legacy.status-im.ui.screens.progress.views :as progress]
    [legacy.status-im.ui.screens.reset-password.views :as reset-password]
    [legacy.status-im.ui.screens.rpc-usage-info :as rpc-usage-info]
    [legacy.status-im.ui.screens.sync-settings.views :as sync-settings]
    [legacy.status-im.ui.screens.wakuv2-settings.edit-node.views :as edit-wakuv2-node]
    [legacy.status-im.ui.screens.wakuv2-settings.views :as wakuv2-settings]
    [react-native.platform :as platform]
    [utils.i18n :as i18n]))

(defn topbar-options
  [title]
  {:elevation        0
   :title            {:color (if (colors/dark?) colors/white colors/black)
                      :text  (i18n/label title)}
   :rightButtonColor (if (colors/dark?) colors/white colors/black)
   :background       {:color (if (colors/dark?) colors/black colors/white)}
   :backButton       {:color           (if (colors/dark?) colors/white colors/black)
                      :id              :legacy-back-button
                      :testID          :back-button
                      :visible         true
                      :popStackOnPress false}})

(defn screens
  []
  [;;PROGRESS
   {:name      :progress
    :options   {:insets {:top? true}
                :theme  :dark}
    :component progress/progress}

   ;;COMMUNITY
   {:name      :legacy-community-members
    :options   {:insets {:top? true}}
    :component members/legacy-members-container}

   {:name      :legacy-invite-people-community
    :options   {:insets {:bottom? true :top? true}}
    :component communities.invite/legacy-invite}

   ;;SETTINGS
   {:name      :legacy-notifications
    :options   {:topBar {:visible false}
                :insets {:top? platform/android?}}
    :component notifications-settings/notifications-settings}

   {:name      :legacy-appearance
    :options   {:topBar {:visible false}
                :insets {:top? platform/android?}}
    :component appearance/appearance-view}

   ;; ADVANCED
   {:name      :legacy-advanced-settings
    :options   {:topBar {:visible false}
                :insets {:top? platform/android?}}
    :component advanced-settings/advanced-settings}

   {:name      :legacy-:og-level-settings
    :options   {:topBar {:visible false}
                :insets {:top? platform/android?}}
    :component log-level-settings/log-level-settings}

   ;; LEGACY SETTINGS
   {:name      :legacy-settings
    :options   {:topBar {:visible false}
                :insets {:top? platform/android?}}
    :component profile.user/legacy-settings}

   ;; PRIVACY AND SECURITY
   {:name      :legacy-privacy-and-security
    :options   {:topBar {:visible false}
                :insets {:top? platform/android?}}
    :component privacy-and-security/privacy-and-security}

   {:name      :legacy-link-previews-settings
    :options   {:topBar {:visible false}
                :insets {:top? platform/android?}}
    :component link-previews-settings/link-previews-settings}

   ;; SYNC
   {:name      :legacy-sync-settings
    :options   {:topBar {:visible false}
                :insets {:top? platform/android?}}
    :component sync-settings/sync-settings}

   ;; ABOUT
   {:name      :about-app
    :options   {:topBar {:visible false}
                :insets {:top? platform/android?}}
    :component about-app/about-app}

   ;; STATUS HELP
   {:name      :help-center
    :options   {:topBar {:visible false}
                :insets {:top? platform/android?}}
    :component help-center/help-center}

   {:name      :glossary
    :options   {:topBar {:visible false}
                :insets {:top? platform/android?}}
    :component glossary/glossary}

   {:name      :bug-report
    :options   {:topBar {:visible false}
                :insets {:top? platform/android?}}
    :component bug-report/bug-report}

   ;; OTHER
   {:name      :wakuv2-settings
    :options   {:insets {:top? true}}
    :component wakuv2-settings/wakuv2-settings}
   {:name      :edit-wakuv2-node
    :options   {:insets {:top? true}}
    :component edit-wakuv2-node/edit-node}
   {:name      :installations
    :options   {:topBar (topbar-options (i18n/label :t/devices))
                :insets {:top? true}}
    :component pairing/installations}
   {:name      :offline-messaging-settings
    :options   {:insets {:top? true}}
    :component offline-messaging-settings/offline-messaging-settings}
   {:name      :edit-mailserver
    :options   {:insets {:top? true}}
    :component edit-mailserver/edit-mailserver}
   {:name      :dapps-permissions
    :options   {:topBar (topbar-options :t/dapps-permissions)
                :insets {:top? true}}
    :component dapps-permissions/dapps-permissions}
   {:name      :messages-from-contacts-only
    :options   {:topBar (topbar-options :t/accept-new-chats-from)
                :insets {:top? true}}
    :component messages-from-contacts-only/messages-from-contacts-only-view}
   {:name      :privacy-and-security-profile-pic-show-to
    :options   {:topbar (topbar-options :t/show-profile-pictures-to)
                :insets {:top? true}}
    :component privacy-and-security/profile-pic-show-to}
   {:name      :privacy-and-security-profile-pic
    :options   {:topBar (topbar-options :t/show-profile-pictures)
                :insets {:top? true}}
    :component privacy-and-security/profile-pic}
   {:name      :manage-dapps-permissions
    :options   {:insets {:top? true}}
    :component dapps-permissions/manage}
   {:name      :rpc-usage-info
    :options   {:topBar (topbar-options :t/rpc-usage-info)
                :insets {:top? true}}
    :component rpc-usage-info/usage-info}
   {:name      :peers-stats
    :options   {:topBar (topbar-options :t/peers-stats)
                :insets {:top? true}}
    :component peers-stats/peers-stats}
   {:name      :fleet-settings
    :options   {:topBar (topbar-options :t/fleet-settings)
                :insets {:top? true}}
    :component fleet-settings/fleet-settings}
   {:name      :mobile-network-settings
    :options   {:topBar (topbar-options :t/mobile-network-settings)
                :insets {:top? true}}
    :component mobile-network-settings/mobile-network-settings}
   {:name      :backup-settings
    :options   {:topBar (topbar-options :t/backup-settings)
                :insets {:top? true}}
    :component backup-settings/backup-settings}
   {:name      :backup-seed
    :options   {:insets {:top? platform/android? :bottom? true}}
    :component profile.seed/backup-seed}
   {:name      :reset-password
    :options   {:topBar (topbar-options :t/reset-password)
                :insets {:top? true}}
    :component reset-password/reset-password}
   {:name      :delete-profile
    :insets    {:bottom? true}
    :component delete-profile/delete-profile}
   {:name      :default-sync-period-settings
    :options   {:topBar (topbar-options :t/default-sync-period)
                :insets {:top? true}}
    :component default-sync-period-settings/default-sync-period-settings}])
