(ns status-im.navigation.screens
  (:require
    [legacy.status-im.ui.screens.screens :as old-screens]
    [status-im.common.emoji-picker.view :as emoji-picker]
    [status-im.common.lightbox.view :as lightbox]
    [status-im.config :as config]
    [status-im.contexts.chat.group-details.view :as group-details]
    [status-im.contexts.chat.home.add-new-contact.scan.scan-profile-qr-page :as scan-profile-qr-page]
    [status-im.contexts.chat.home.add-new-contact.views :as add-new-contact]
    [status-im.contexts.chat.home.new-chat.view :as new-chat]
    [status-im.contexts.chat.messenger.camera.view :as camera-screen]
    [status-im.contexts.chat.messenger.messages.view :as chat]
    [status-im.contexts.chat.messenger.photo-selector.view :as photo-selector]
    [status-im.contexts.communities.actions.accounts-selection.view :as communities.accounts-selection]
    [status-im.contexts.communities.actions.addresses-for-permissions.view :as
     addresses-for-permissions]
    [status-im.contexts.communities.actions.airdrop-addresses.view :as airdrop-addresses]
    [status-im.contexts.communities.actions.request-to-join.view :as join-menu]
    [status-im.contexts.communities.discover.view :as communities.discover]
    [status-im.contexts.communities.overview.view :as communities.overview]
    [status-im.contexts.onboarding.create-password.view :as create-password]
    [status-im.contexts.onboarding.create-profile.view :as create-profile]
    [status-im.contexts.onboarding.enable-biometrics.view :as enable-biometrics]
    [status-im.contexts.onboarding.enable-notifications.view :as enable-notifications]
    [status-im.contexts.onboarding.enter-seed-phrase.view :as enter-seed-phrase]
    [status-im.contexts.onboarding.generating-keys.view :as generating-keys]
    [status-im.contexts.onboarding.identifiers.view :as identifiers]
    [status-im.contexts.onboarding.intro.view :as intro]
    [status-im.contexts.onboarding.new-to-status.view :as new-to-status]
    [status-im.contexts.onboarding.sign-in.view :as sign-in]
    [status-im.contexts.onboarding.syncing.progress.view :as syncing-devices]
    [status-im.contexts.onboarding.syncing.results.view :as syncing-results]
    [status-im.contexts.onboarding.welcome.view :as welcome]
    [status-im.contexts.preview.feature-flags.view :as feature-flags]
    [status-im.contexts.preview.quo.component-preview.view :as component-preview]
    [status-im.contexts.preview.quo.main :as quo.preview]
    [status-im.contexts.preview.status-im.main :as status-im-preview]
    [status-im.contexts.profile.edit.accent-colour.view :as edit-accent-colour]
    [status-im.contexts.profile.edit.bio.view :as edit-bio]
    [status-im.contexts.profile.edit.name.view :as edit-name]
    [status-im.contexts.profile.edit.view :as edit-profile]
    [status-im.contexts.profile.profiles.view :as profiles]
    [status-im.contexts.profile.settings.screens.password.view :as settings-password]
    [status-im.contexts.profile.settings.view :as settings]
    [status-im.contexts.shell.activity-center.view :as activity-center]
    [status-im.contexts.shell.jump-to.view :as shell]
    [status-im.contexts.shell.qr-reader.view :as shell-qr-reader]
    [status-im.contexts.shell.share.view :as share]
    [status-im.contexts.syncing.find-sync-code.view :as find-sync-code]
    [status-im.contexts.syncing.how-to-pair.view :as how-to-pair]
    [status-im.contexts.syncing.scan-sync-code-page.view :as scan-sync-code-page]
    [status-im.contexts.syncing.setup-syncing.view :as settings-setup-syncing]
    [status-im.contexts.syncing.syncing-devices-list.view :as settings-syncing]
    [status-im.contexts.wallet.account.bridge-send.view :as bridge-send]
    [status-im.contexts.wallet.account.bridge-to.view :as bridge-to]
    [status-im.contexts.wallet.account.bridge.view :as bridge]
    [status-im.contexts.wallet.account.view :as wallet-accounts]
    [status-im.contexts.wallet.add-address-to-watch.confirm-address.view :as confirm-address-to-watch]
    [status-im.contexts.wallet.add-address-to-watch.view :as add-address-to-watch]
    [status-im.contexts.wallet.collectible.view :as wallet-collectible]
    [status-im.contexts.wallet.create-account.edit-derivation-path.view :as wallet-edit-derivation-path]
    [status-im.contexts.wallet.create-account.new-keypair.backup-recovery-phrase.view :as
     wallet-backup-recovery-phrase]
    [status-im.contexts.wallet.create-account.new-keypair.check-your-backup.view :as
     wallet-check-your-backup]
    [status-im.contexts.wallet.create-account.new-keypair.keypair-name.view :as wallet-keypair-name]
    [status-im.contexts.wallet.create-account.select-keypair.view :as wallet-select-keypair]
    [status-im.contexts.wallet.create-account.view :as wallet-create-account]
    [status-im.contexts.wallet.edit-account.view :as wallet-edit-account]
    [status-im.contexts.wallet.saved-addresses.view :as wallet-saved-addresses]
    [status-im.contexts.wallet.scan-account.view :as scan-address]
    [status-im.contexts.wallet.send.select-address.view :as wallet-select-address]
    [status-im.contexts.wallet.send.select-asset.view :as wallet-select-asset]
    [status-im.contexts.wallet.send.send-amount.view :as wallet-send-input-amount]
    [status-im.contexts.wallet.send.transaction-confirmation.view :as wallet-transaction-confirmation]
    [status-im.contexts.wallet.send.transaction-progress.view :as wallet-transaction-progress]
    [status-im.contexts.wallet.share-address.view :as wallet-share-address]
    [status-im.navigation.options :as options]
    [status-im.navigation.transitions :as transitions]))

(defn screens
  []
  (concat
   (old-screens/screens)

   [{:name      :activity-center
     :options   options/transparent-screen-options
     :component activity-center/view}

    {:name      :share-shell
     :options   options/transparent-screen-options
     :component share/view}

    {:name      :shell-stack
     :component shell/shell-stack}

    {:name      :shell-qr-reader
     :options   (assoc options/dark-screen :modalPresentationStyle :overCurrentContext)
     :component shell-qr-reader/view}

    {:name      :chat
     :options   {:insets     {:top? true}
                 :popGesture false}
     :component chat/chat}

    {:name      :start-a-new-chat
     :options   {:sheet? true}
     :component new-chat/view}

    {:name      :group-add-manage-members
     :options   {:sheet? true}
     :component group-details/add-manage-members}

    {:name      :community-requests-to-join
     :options   {:sheet? true}
     :component join-menu/view}

    {:name      :community-account-selection
     :options   {:sheet? true}
     :component communities.accounts-selection/view}

    {:name      :addresses-for-permissions
     :options   {:sheet? true}
     :component addresses-for-permissions/view}

    {:name      :airdrop-addresses
     :options   {:sheet? true}
     :component airdrop-addresses/view}

    {:name      :lightbox
     :options   options/lightbox
     :component lightbox/lightbox}

    {:name      :photo-selector
     :options   (merge {:sheet? true} (options/statusbar-and-navbar-root))
     :component photo-selector/photo-selector}

    {:name      :camera-screen
     :options   options/camera-screen
     :component camera-screen/camera-screen}

    {:name      :new-contact
     :options   {:sheet? true}
     :component add-new-contact/new-contact}

    {:name      :how-to-pair
     :options   (assoc options/dark-screen :sheet? true)
     :component how-to-pair/view}

    {:name      :find-sync-code
     :options   (assoc options/dark-screen :sheet? true)
     :component find-sync-code/view}

    {:name      :discover-communities
     :component communities.discover/view}

    {:name      :community-overview
     :component communities.overview/view}

    {:name      :settings
     :options   options/transparent-screen-options
     :component settings/view}

    {:name      :settings-syncing
     :options   (merge options/dark-screen {:insets {:top? true}})
     :component settings-syncing/view}

    {:name      :settings-setup-syncing
     :options   (merge options/dark-screen {:insets {:top? true}})
     :component settings-setup-syncing/view}

    ;; Onboarding
    {:name      :intro
     :options   {:theme :dark}
     :on-focus  [:onboarding/overlay-dismiss]
     :component intro/view}

    {:name      :profiles
     :options   {:theme  :dark
                 :layout options/onboarding-layout}
     :on-focus  [:onboarding/overlay-dismiss]
     :component profiles/view}

    {:name      :edit-profile
     :options   options/transparent-modal-screen-options
     :component edit-profile/view}

    {:name      :edit-accent-colour
     :options   options/transparent-modal-screen-options
     :component edit-accent-colour/view}

    {:name      :edit-name
     :options   options/transparent-modal-screen-options
     :component edit-name/view}

    {:name      :edit-bio
     :options   options/transparent-modal-screen-options
     :component edit-bio/view}

    {:name      :new-to-status
     :options   {:theme                  :dark
                 :layout                 options/onboarding-transparent-layout
                 :animations             (merge
                                          transitions/new-to-status-modal-animations
                                          transitions/push-animations-for-transparent-background)
                 :popGesture             false
                 :modalPresentationStyle :overCurrentContext}
     :component new-to-status/new-to-status}

    {:name      :create-profile
     :options   {:theme      :dark
                 :layout     options/onboarding-transparent-layout
                 :animations transitions/push-animations-for-transparent-background
                 :popGesture false}
     :component create-profile/create-profile}

    {:name      :create-profile-password
     :options   {:theme      :dark
                 :insets     {:top false}
                 :layout     options/onboarding-transparent-layout
                 :animations transitions/push-animations-for-transparent-background
                 :popGesture false}
     :component create-password/create-password}

    {:name      :enable-biometrics
     :options   (merge
                 (options/statusbar-and-navbar-root)
                 {:theme                  :dark
                  :layout                 options/onboarding-transparent-layout
                  :animations             (merge
                                           transitions/new-to-status-modal-animations
                                           transitions/push-animations-for-transparent-background)
                  :popGesture             false
                  :modalPresentationStyle :overCurrentContext
                  :hardwareBackButton     {:dismissModalOnPress false
                                           :popStackOnPress     false}})
     :component enable-biometrics/view}

    {:name      :generating-keys
     :options   {:theme              :dark
                 :layout             options/onboarding-transparent-layout
                 :animations         transitions/push-animations-for-transparent-background
                 :popGesture         false
                 :hardwareBackButton {:dismissModalOnPress false
                                      :popStackOnPress     false}}
     :component generating-keys/generating-keys}

    {:name      :enter-seed-phrase
     :options   {:theme      :dark
                 :layout     options/onboarding-transparent-layout
                 :animations transitions/push-animations-for-transparent-background
                 :popGesture false}
     :component enter-seed-phrase/enter-seed-phrase}

    {:name      :enable-notifications
     :options   (merge
                 (options/statusbar-and-navbar-root)
                 {:theme                  :dark
                  :layout                 options/onboarding-transparent-layout
                  :animations             (merge
                                           transitions/new-to-status-modal-animations
                                           transitions/push-animations-for-transparent-background)
                  :modalPresentationStyle :overCurrentContext})
     :component enable-notifications/view}

    {:name      :identifiers
     :component identifiers/view
     :options   {:theme              :dark
                 :layout             options/onboarding-transparent-layout
                 :animations         transitions/push-animations-for-transparent-background
                 :popGesture         false
                 :hardwareBackButton {:dismissModalOnPress false
                                      :popStackOnPress     false}}}

    {:name      :scan-sync-code-page
     :options   options/dark-screen
     :component scan-sync-code-page/view}

    {:name      :sign-in-intro
     :options   {:layout                 options/onboarding-transparent-layout
                 :animations             (merge
                                          transitions/sign-in-modal-animations
                                          transitions/push-animations-for-transparent-background)
                 :modalPresentationStyle :overCurrentContext}
     :component sign-in/animated-view}

    {:name      :sign-in
     :options   {:theme                  :dark
                 :modalPresentationStyle :overCurrentContext
                 :layout                 options/onboarding-layout}
     :component sign-in/view}

    {:name      :syncing-progress
     :options   (merge
                 (options/statusbar-and-navbar-root)
                 {:theme      :dark
                  :layout     options/onboarding-layout
                  :popGesture false})
     :component syncing-devices/view}

    {:name      :syncing-progress-intro
     :options   {:theme      :dark
                 :layout     options/onboarding-transparent-layout
                 :animations transitions/push-animations-for-transparent-background
                 :popGesture false}
     :component syncing-devices/view-onboarding}

    {:name      :syncing-results
     :options   {:theme :dark}
     :component syncing-results/view}

    {:name      :welcome
     :options   (merge
                 (options/statusbar-and-navbar-root)
                 {:theme      :dark
                  :layout     options/onboarding-transparent-layout
                  :animations transitions/push-animations-for-transparent-background})
     :component welcome/view}

    {:name      :emoji-picker
     :options   {:sheet? true}
     :component emoji-picker/view}

    {:name      :wallet-accounts
     :options   {:insets             {:top? true}
                 :popGesture         false
                 :hardwareBackButton {:dismissModalOnPress false
                                      :popStackOnPress     false}}
     :component wallet-accounts/view}

    {:name      :wallet-edit-account
     :component wallet-edit-account/view}

    {:name      :add-address-to-watch
     :options   {:insets {:top? true}}
     :component add-address-to-watch/view}

    {:name      :confirm-address-to-watch
     :component confirm-address-to-watch/view}

    {:name      :wallet-bridge
     :options   {:insets                 {:top? true}
                 :modalPresentationStyle :overCurrentContext}
     :component bridge/view}

    {:name      :wallet-bridge-to
     :options   {:insets {:top? true}}
     :component bridge-to/view}

    {:name      :wallet-bridge-send
     :options   {:insets {:top? true}}
     :component bridge-send/view}

    {:name      :wallet-edit-derivation-path
     :component wallet-edit-derivation-path/view}

    {:name      :wallet-collectible
     :component wallet-collectible/view}

    {:name      :wallet-select-keypair
     :options   {:insets {:top? true :bottom? true}}
     :component wallet-select-keypair/view}

    {:name      :wallet-create-account
     :options   {:insets {:top? true}}
     :component wallet-create-account/view}

    {:name      :wallet-backup-recovery-phrase
     :options   {:insets {:top? true :bottom? true}}
     :component wallet-backup-recovery-phrase/view}

    {:name      :wallet-check-your-backup
     :options   {:insets {:top? true :bottom? true}}
     :component wallet-check-your-backup/view}

    {:name      :wallet-keypair-name
     :options   {:insets {:top? true :bottom? true}}
     :component wallet-keypair-name/view}

    {:name      :wallet-share-address
     :options   options/transparent-screen-options
     :component wallet-share-address/view}

    {:name      :wallet-saved-addresses
     :component wallet-saved-addresses/view}

    {:name      :wallet-send-input-amount
     :options   {:modalPresentationStyle :overCurrentContext
                 :insets                 {:top?    true
                                          :bottom? true}}
     :component wallet-send-input-amount/view}

    {:name      :wallet-select-address
     :options   {:modalPresentationStyle :overCurrentContext
                 :insets                 {:top? true}}
     :component wallet-select-address/view}

    {:name      :wallet-select-asset
     :options   {:insets {:top? true}}
     :component wallet-select-asset/view}

    {:name      :wallet-transaction-confirmation
     :component wallet-transaction-confirmation/view}

    {:name      :wallet-transaction-progress
     :component wallet-transaction-progress/view}

    {:name      :scan-address
     :options   (merge
                 options/dark-screen
                 {:modalPresentationStyle :overCurrentContext})
     :component scan-address/view}

    {:name      :scan-profile-qr-code
     :options   (merge
                 options/dark-screen
                 {:modalPresentationStyle :overCurrentContext})
     :component scan-profile-qr-page/view}

    ;; Settings

    {:name      :settings-password
     :options   options/transparent-screen-options
     :component settings-password/view}]

   (when js/goog.DEBUG
     [{:name      :dev-component-preview
       :options   {:sheet? true}
       :component component-preview/view}])

   (when config/quo-preview-enabled?
     quo.preview/screens)

   (when config/quo-preview-enabled?
     quo.preview/main-screens)

   (when config/quo-preview-enabled?
     status-im-preview/screens)

   (when config/quo-preview-enabled?
     status-im-preview/main-screens)

   (when config/quo-preview-enabled?
     [{:name      :feature-flags
       :options   {:insets {:top? true}}
       :component feature-flags/view}])))
