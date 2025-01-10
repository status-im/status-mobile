(ns legacy.status-im.ui.screens.screens
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.screens.advanced-settings.views :as advanced-settings]
    [legacy.status-im.ui.screens.appearance.views :as appearance]
    [legacy.status-im.ui.screens.backup-settings.view :as backup-settings]
    [legacy.status-im.ui.screens.bug-report :as bug-report]
    [legacy.status-im.ui.screens.communities.members :as members]
    [legacy.status-im.ui.screens.default-sync-period-settings.view :as default-sync-period-settings]
    [legacy.status-im.ui.screens.fleet-settings.views :as fleet-settings]
    [legacy.status-im.ui.screens.glossary.view :as glossary]
    [legacy.status-im.ui.screens.help-center.views :as help-center]
    [legacy.status-im.ui.screens.log-level-settings.views :as log-level-settings]
    [legacy.status-im.ui.screens.notifications-settings.views :as notifications-settings]
    [legacy.status-im.ui.screens.offline-messaging-settings.views :as offline-messaging-settings]
    [legacy.status-im.ui.screens.pairing.views :as pairing]
    [legacy.status-im.ui.screens.peers-stats :as peers-stats]
    [legacy.status-im.ui.screens.profile.seed.views :as profile.seed]
    [legacy.status-im.ui.screens.profile.user.views :as profile.user]
    [legacy.status-im.ui.screens.progress.views :as progress]
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

   ;; SYNC
   {:name      :legacy-sync-settings
    :options   {:topBar {:visible false}
                :insets {:top? platform/android?}}
    :component sync-settings/sync-settings}

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
   {:name      :backup-settings
    :options   {:topBar (topbar-options :t/backup-settings)
                :insets {:top? true}}
    :component backup-settings/backup-settings}
   {:name      :backup-seed
    :options   {:insets {:top? platform/android? :bottom? true}}
    :component profile.seed/backup-seed}
   {:name      :default-sync-period-settings
    :options   {:topBar (topbar-options :t/default-sync-period)
                :insets {:top? true}}
    :component default-sync-period-settings/default-sync-period-settings}])
