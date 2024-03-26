(ns legacy.status-im.ui.screens.screens
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.screens.about-app.views :as about-app]
    [legacy.status-im.ui.screens.advanced-settings.views :as advanced-settings]
    [legacy.status-im.ui.screens.appearance.views :as appearance]
    [legacy.status-im.ui.screens.backup-settings.view :as backup-settings]
    [legacy.status-im.ui.screens.bootnodes-settings.edit-bootnode.views :as edit-bootnode]
    [legacy.status-im.ui.screens.bootnodes-settings.views :as bootnodes-settings]
    [legacy.status-im.ui.screens.browser.bookmarks.views :as bookmarks]
    [legacy.status-im.ui.screens.bug-report :as bug-report]
    [legacy.status-im.ui.screens.communities.invite :as communities.invite]
    [legacy.status-im.ui.screens.communities.members :as members]
    [legacy.status-im.ui.screens.contacts-list.views :as contacts-list]
    [legacy.status-im.ui.screens.currency-settings.views :as currency-settings]
    [legacy.status-im.ui.screens.dapps-permissions.views :as dapps-permissions]
    [legacy.status-im.ui.screens.default-sync-period-settings.view :as default-sync-period-settings]
    [legacy.status-im.ui.screens.ens.views :as ens]
    [legacy.status-im.ui.screens.fleet-settings.views :as fleet-settings]
    [legacy.status-im.ui.screens.glossary.view :as glossary]
    [legacy.status-im.ui.screens.group.views :as group-chat]
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
    [legacy.status-im.ui.screens.profile.contact.views :as contact]
    [legacy.status-im.ui.screens.profile.group-chat.views :as profile.group-chat]
    [legacy.status-im.ui.screens.profile.seed.views :as profile.seed]
    [legacy.status-im.ui.screens.profile.user.views :as profile.user]
    [legacy.status-im.ui.screens.progress.views :as progress]
    [legacy.status-im.ui.screens.qr-scanner.views :as qr-scanner]
    [legacy.status-im.ui.screens.reset-password.views :as reset-password]
    [legacy.status-im.ui.screens.rpc-usage-info :as rpc-usage-info]
    [legacy.status-im.ui.screens.stickers.views :as stickers]
    [legacy.status-im.ui.screens.sync-settings.views :as sync-settings]
    [legacy.status-im.ui.screens.wakuv2-settings.edit-node.views :as edit-wakuv2-node]
    [legacy.status-im.ui.screens.wakuv2-settings.views :as wakuv2-settings]
    [status-im.contexts.chat.group-details.view :as group-details]
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
  [;;INTRO, ONBOARDING, LOGIN

   {:name      :progress
    :options   {:insets {:top? true}}
    :component progress/progress}

   ;;CHAT
   {:name      :group-chat-profile
    ;;TODO animated-header
    :options   {:insets {:top? true}}
    :component group-details/group-details}
   {:name      :group-chat-invite
    ;;TODO parameter in the event
    :options   {:insets {:top? true}}
    :component profile.group-chat/group-chat-invite}
   {:name      :legacy-community-members
    ;;TODO custom subtitle
    :options   {:insets {:top? true}}
    :component members/legacy-members-container}

   {:name      :stickers
    :options   {:insets {:top? true}
                :topBar (topbar-options :t/sticker-market)}
    :component stickers/packs}

   {:name      :stickers-pack
    :options   {:insets {:top? true}}
    :component stickers/pack}

   {:name      :currency-settings
    :options   {:topBar (topbar-options :t/main-currency)
                :insets {:top? true}}
    :component currency-settings/currency-settings}

   ;;PROFILE

   {:name      :my-profile
    :options   {:topBar {:visible false}}
    :component profile.user/my-profile}
   {:name      :contacts-list
    :options   {:topBar (topbar-options :t/contacts)
                :insets {:top? true}}
    :component contacts-list/contacts-list}
   {:name      :ens-main
    :options   {:topBar (topbar-options :t/ens-usernames)
                :insets {:top? true}}
    :component ens/main}
   {:name      :ens-search
    :options   {:topBar (topbar-options :t/ens-your-username)
                :insets {:top? true}}
    :component ens/search}
   {:name      :ens-checkout
    :options   {:topBar (topbar-options :t/ens-your-username)
                :insets {:top? true}}
    :component ens/checkout}
   {:name      :ens-confirmation
    :options   {:topBar (topbar-options :t/ens-your-username)
                :insets {:top? true}}
    :component ens/confirmation}
   {:name      :ens-terms
    :options   {:topBar (topbar-options :t/ens-terms-registration)
                :insets {:top? true}}
    :component ens/terms}
   {:name      :ens-name-details
    ;;TODO dynamic title
    :options   {:insets {:top? true}}
    :component ens/name-details}
   {:name      :blocked-users-list
    :options   {:topBar (topbar-options :t/blocked-users)
                :insets {:top? true}}
    :component contacts-list/blocked-users-list}
   {:name      :wakuv2-settings
    ;;TODO dynamic title
    :options   {:insets {:top? true}}
    :component wakuv2-settings/wakuv2-settings}
   {:name      :edit-wakuv2-node
    ;;TODO dynamic title
    :options   {:insets {:top? true}}
    :component edit-wakuv2-node/edit-node}
   {:name      :bootnodes-settings
    ;;TODO dynamic title
    :options   {:insets {:top? true}}
    :component bootnodes-settings/bootnodes-settings}
   {:name      :installations
    :options   {:topBar (topbar-options (i18n/label :t/devices))
                :insets {:top? true}}
    :component pairing/installations}
   {:name      :edit-bootnode
    ;;TODO dynamic title
    :options   {:insets {:top? true}}
    :component edit-bootnode/edit-bootnode}
   {:name      :offline-messaging-settings
    ;;TODO dynamic title
    :options   {:insets {:top? true}}
    :component offline-messaging-settings/offline-messaging-settings}
   {:name      :edit-mailserver
    ;;TODO dynamic title
    :options   {:insets {:top? true}}
    :component edit-mailserver/edit-mailserver}
   {:name      :dapps-permissions
    :options   {:topBar (topbar-options :t/dapps-permissions)
                :insets {:top? true}}
    :component dapps-permissions/dapps-permissions}
   {:name      :privacy-and-security
    :options   {:topBar (topbar-options :t/privacy-and-security)
                :insets {:top? true}}
    :component privacy-and-security/privacy-and-security}
   {:name      :messages-from-contacts-only
    :options   {:topBar (topbar-options :t/accept-new-chats-from)
                :insets {:top? true}}
    :component messages-from-contacts-only/messages-from-contacts-only-view}
   {:name      :appearance
    :options   {:topBar (topbar-options :t/appearance)
                :insets {:top? true}}
    :component appearance/appearance-view}
   {:name      :privacy-and-security-profile-pic-show-to
    :options   {:topbar (topbar-options :t/show-profile-pictures-to)
                :insets {:top? true}}
    :component privacy-and-security/profile-pic-show-to}
   {:name      :privacy-and-security-profile-pic
    :options   {:topBar (topbar-options :t/show-profile-pictures)
                :insets {:top? true}}
    :component privacy-and-security/profile-pic}
   {:name      :notifications
    :options   {:topBar (topbar-options :t/notification-settings)
                :insets {:top? true}}
    :component notifications-settings/notifications-settings}
   {:name      :sync-settings
    :options   {:topBar (topbar-options :t/sync-settings)
                :insets {:top? true}}
    :component sync-settings/sync-settings}
   {:name      :advanced-settings
    :options   {:topBar (topbar-options :t/advanced)
                :insets {:top? true}}
    :component advanced-settings/advanced-settings}
   {:name      :help-center
    :options   {:topBar (topbar-options :t/need-help)
                :insets {:top? true}}
    :component help-center/help-center}
   {:name      :glossary
    :options   {:topBar (topbar-options :t/glossary)
                :insets {:top? true}}
    :component glossary/glossary}
   {:name      :about-app
    :options   {:topBar (topbar-options :t/about-app)
                :insets {:top? true}}
    :component about-app/about-app}
   {:name      :privacy-policy
    :options   {:topBar (topbar-options :t/privacy-policy)
                :insets {:top? true}}
    :component about-app/privacy-policy}
   {:name      :terms-of-service
    :options   {:topBar (topbar-options :t/terms-of-service)
                :insets {:top? true}}
    :component about-app/tos}
   {:name      :principles
    :options   {:topBar (topbar-options :t/principles)
                :insets {:top? true}}
    :component about-app/principles}
   {:name      :manage-dapps-permissions
    ;;TODO dynamic title
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
   {:name      :log-level-settings
    :options   {:topBar (topbar-options :t/log-level-settings)
                :insets {:top? true}}
    :component log-level-settings/log-level-settings}
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
    ;;TODO dynamic navigation
    :options   {:insets {:top? true}}
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
    :component default-sync-period-settings/default-sync-period-settings}

   ;;MODALS

   ;[Chat] Link preview settings

   {:name      :link-previews-settings
    :options   {:topBar (topbar-options :t/chat-link-previews)
                :insets {:top? true}}
    :component link-previews-settings/link-previews-settings}

   ;[Chat] Edit nickname
   {:name      :nickname
    ;;TODO dyn subtitle
    :options   {:topBar {:visible false}
                :insets {:bottom? true
                         :top?    true}}
    :component contact/nickname-view}

   ;[Group chat] Add participants
   {:name      :add-participants-toggle-list
    :on-focus  [:group/add-participants-toggle-list]
    ;;TODO dyn subtitle
    :options   {:topBar {:visible false}
                :insets {:bottom? true
                         :top?    true}}
    :component group-chat/add-participants-toggle-list}

   ;[Communities] Invite people
   {:name      :legacy-invite-people-community
    ;;TODO dyn title
    :options   {:insets {:bottom? true
                         :top?    true}}
    :component communities.invite/legacy-invite}

   ;QR Scanner
   {:name      :qr-scanner
    ;;TODO custom topbar
    :options   {:topBar        {:visible false}
                :navigationBar {:backgroundColor colors/black-persist}
                :statusBar     {:backgroundColor colors/black-persist
                                :style           :light}}
    :component qr-scanner/qr-scanner}

   ;;TODO WHY MODAL?
   ;[Profile] Notifications settings
   {:name      :notifications-settings
    :options   {:topBar             (topbar-options :t/notification-settings)
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}
                :insets             {:bottom? true
                                     :top?    true}}
    :component notifications-settings/notifications-settings}

   ;[Browser] New bookmark
   {:name      :new-bookmark
    ;;TODO dynamic title
    :options   {:insets {:bottom? true
                         :top?    true}}
    :component bookmarks/new-bookmark}

   ;Profile
   {:name      :profile
    ;;TODO custom toolbar
    :options   {:insets {:bottom? true
                         :top?    true}}
    :component contact/profile-view}

   ;; BUG REPORT
   {:name      :bug-report
    :options   {:insets {:top? true}}
    :component bug-report/bug-report}])
