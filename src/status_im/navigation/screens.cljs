(ns status-im.navigation.screens
  (:require
    [legacy.status-im.ui.screens.screens :as old-screens]
    [quo.foundations.colors :as colors]
    [status-im.common.emoji-picker.view :as emoji-picker]
    [status-im.common.enter-seed-phrase.view :as enter-seed-phrase]
    [status-im.common.lightbox.view :as lightbox]
    [status-im.config :as config]
    [status-im.contexts.chat.group-create.view :as group-create]
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
    [status-im.contexts.communities.actions.channel-view-details.view :as
     channel-view-channel-members-and-details]
    [status-im.contexts.communities.actions.invite-contacts.view :as communities.invite]
    [status-im.contexts.communities.actions.request-to-join.view :as join-menu]
    [status-im.contexts.communities.actions.share-community-channel.view :as share-community-channel]
    [status-im.contexts.communities.actions.share-community.view :as share-community]
    [status-im.contexts.communities.discover.view :as communities.discover]
    [status-im.contexts.communities.overview.view :as communities.overview]
    [status-im.contexts.onboarding.create-or-sync-profile.view :as create-or-sync-profile]
    [status-im.contexts.onboarding.create-password.view :as create-password]
    [status-im.contexts.onboarding.create-profile.view :as create-profile]
    [status-im.contexts.onboarding.enable-biometrics.view :as enable-biometrics]
    [status-im.contexts.onboarding.enable-notifications.view :as enable-notifications]
    [status-im.contexts.onboarding.generating-keys.view :as generating-keys]
    [status-im.contexts.onboarding.identifiers.view :as identifiers]
    [status-im.contexts.onboarding.intro.view :as intro]
    [status-im.contexts.onboarding.preparing-status.view :as preparing-status]
    [status-im.contexts.onboarding.sign-in.view :as sign-in]
    [status-im.contexts.onboarding.syncing.progress.view :as syncing-devices]
    [status-im.contexts.onboarding.syncing.results.view :as syncing-results]
    [status-im.contexts.preview.feature-flags.view :as feature-flags]
    [status-im.contexts.preview.quo.component-preview.view :as component-preview]
    [status-im.contexts.preview.quo.main :as quo.preview]
    [status-im.contexts.preview.status-im.main :as status-im-preview]
    [status-im.contexts.profile.contact.share.view :as share-contact]
    [status-im.contexts.profile.contact.view :as contact-profile]
    [status-im.contexts.profile.edit.accent-colour.view :as edit-accent-colour]
    [status-im.contexts.profile.edit.bio.view :as edit-bio]
    [status-im.contexts.profile.edit.name.view :as edit-name]
    [status-im.contexts.profile.edit.view :as edit-profile]
    [status-im.contexts.profile.profiles.view :as profiles]
    [status-im.contexts.profile.settings.screens.messages.blocked-users.view :as
     settings.blocked-users]
    [status-im.contexts.profile.settings.screens.messages.view :as settings.messages]
    [status-im.contexts.profile.settings.screens.password.change-password.loading :as
     change-password-loading]
    [status-im.contexts.profile.settings.screens.password.change-password.view :as change-password]
    [status-im.contexts.profile.settings.screens.password.view :as settings-password]
    [status-im.contexts.profile.settings.screens.syncing.view :as settings.syncing]
    [status-im.contexts.profile.settings.view :as settings]
    [status-im.contexts.settings.language-and-currency.currency.view :as settings.currency-selection]
    [status-im.contexts.settings.language-and-currency.view :as settings.language-and-currency]
    [status-im.contexts.settings.privacy-and-security.share-usage.view :as settings.share-usage]
    [status-im.contexts.settings.privacy-and-security.view :as settings.privacy-and-security]
    [status-im.contexts.settings.wallet.keypairs-and-accounts.missing-keypairs.encrypted-qr.view
     :as encrypted-keypair-qr]
    [status-im.contexts.settings.wallet.keypairs-and-accounts.missing-keypairs.import-private-key.view
     :as missing-keypairs.import-private-key]
    [status-im.contexts.settings.wallet.keypairs-and-accounts.missing-keypairs.import-seed-phrase.view
     :as missing-keypairs.import-seed-phrase]
    [status-im.contexts.settings.wallet.keypairs-and-accounts.missing-keypairs.scan-qr.view
     :as scan-keypair-qr]
    [status-im.contexts.settings.wallet.keypairs-and-accounts.rename.view :as keypair-rename]
    [status-im.contexts.settings.wallet.keypairs-and-accounts.view :as keypairs-and-accounts]
    [status-im.contexts.settings.wallet.network-settings.view :as network-settings]
    [status-im.contexts.settings.wallet.saved-addresses.add-address-to-save.view :as
     wallet-add-address-to-save]
    [status-im.contexts.settings.wallet.saved-addresses.save-address.view :as wallet-save-address]
    [status-im.contexts.settings.wallet.saved-addresses.share-address.view :as
     share-saved-address]
    [status-im.contexts.settings.wallet.saved-addresses.view :as saved-addresses-settings]
    [status-im.contexts.settings.wallet.wallet-options.view :as wallet-options]
    [status-im.contexts.shell.activity-center.view :as activity-center]
    [status-im.contexts.shell.jump-to.view :as shell]
    [status-im.contexts.shell.qr-reader.view :as shell-qr-reader]
    [status-im.contexts.shell.share.view :as share]
    [status-im.contexts.syncing.find-sync-code.view :as find-sync-code]
    [status-im.contexts.syncing.how-to-pair.view :as how-to-pair]
    [status-im.contexts.syncing.scan-sync-code-page.view :as scan-sync-code-page]
    [status-im.contexts.syncing.setup-syncing.view :as settings-setup-syncing]
    [status-im.contexts.syncing.syncing-devices-list.view :as syncing-devices-list]
    [status-im.contexts.wallet.account.edit-account.view :as wallet-edit-account]
    [status-im.contexts.wallet.account.share-address.view :as wallet-share-address]
    [status-im.contexts.wallet.account.view :as wallet-accounts]
    [status-im.contexts.wallet.add-account.add-address-to-watch.confirm-address.view :as
     wallet-confirm-address-to-watch]
    [status-im.contexts.wallet.add-account.add-address-to-watch.view :as wallet-add-address-to-watch]
    [status-im.contexts.wallet.add-account.create-account.edit-derivation-path.view :as
     wallet-edit-derivation-path]
    [status-im.contexts.wallet.add-account.create-account.import-private-key.view :as
     wallet-import-private-key]
    [status-im.contexts.wallet.add-account.create-account.key-pair-name.view :as
     wallet-key-pair-name]
    [status-im.contexts.wallet.add-account.create-account.new-keypair.backup-recovery-phrase.view :as
     wallet-backup-recovery-phrase]
    [status-im.contexts.wallet.add-account.create-account.new-keypair.confirm-backup.view :as
     wallet-confirm-backup]
    [status-im.contexts.wallet.add-account.create-account.select-keypair.view :as wallet-select-keypair]
    [status-im.contexts.wallet.add-account.create-account.view :as wallet-create-account]
    [status-im.contexts.wallet.bridge.bridge-to.view :as wallet-bridge-to]
    [status-im.contexts.wallet.bridge.input-amount.view :as wallet-bridge-input-amount]
    [status-im.contexts.wallet.bridge.select-asset.view :as wallet-bridge-select-asset]
    [status-im.contexts.wallet.collectible.view :as wallet-collectible]
    [status-im.contexts.wallet.common.scan-account.view :as wallet-scan-address]
    [status-im.contexts.wallet.connected-dapps.scan-dapp.view :as wallet-scan-dapp]
    [status-im.contexts.wallet.connected-dapps.view :as wallet-connected-dapps]
    [status-im.contexts.wallet.send.from.view :as wallet-select-from]
    [status-im.contexts.wallet.send.select-address.view :as wallet-select-address]
    [status-im.contexts.wallet.send.select-asset.view :as wallet-select-asset]
    [status-im.contexts.wallet.send.select-collectible-amount.view :as wallet-select-collectible-amount]
    [status-im.contexts.wallet.send.send-amount.view :as wallet-send-input-amount]
    [status-im.contexts.wallet.send.transaction-confirmation.view :as wallet-transaction-confirmation]
    [status-im.contexts.wallet.send.transaction-progress.view :as wallet-transaction-progress]
    [status-im.contexts.wallet.swap.select-asset-to-pay.view :as wallet-swap-select-asset-to-pay]
    [status-im.contexts.wallet.swap.set-spending-cap.view :as wallet-swap-set-spending-cap]
    [status-im.contexts.wallet.swap.setup-swap.view :as wallet-swap-setup-swap]
    [status-im.contexts.wallet.swap.swap-confirmation.view :as wallet-swap-confirmation]
    [status-im.contexts.wallet.swap.swap-proposal.view :as wallet-swap-propasal]
    [status-im.contexts.wallet.wallet-connect.modals.send-transaction.view :as
     wallet-connect-send-transaction]
    [status-im.contexts.wallet.wallet-connect.modals.session-proposal.view :as
     wallet-connect-session-proposal]
    [status-im.contexts.wallet.wallet-connect.modals.sign-message.view :as wallet-connect-sign-message]
    [status-im.contexts.wallet.wallet-connect.modals.sign-transaction.view :as
     wallet-connect-sign-transaction]
    [status-im.navigation.options :as options]
    [status-im.navigation.transitions :as transitions]))

(defn chat-screens
  []
  [{:name      :start-a-new-chat
    :options   {:sheet? true}
    :component new-chat/view}

   {:name      :chat
    :options   {:popGesture false
                :animations transitions/stack-transition-from-bottom}
    :component chat/chat}

   {:name      :group-create
    :options   {:sheet?           true
                :skip-background? true}
    :component group-create/view}

   {:name      :group-details
    :component group-details/group-details}

   {:name      :group-add-manage-members
    :options   {:sheet? true}
    :component group-details/add-manage-members}])

(defn community-screens
  []
  [{:name      :discover-communities
    :component communities.discover/view}

   {:name      :community-overview
    :options   {:animations transitions/stack-transition-from-bottom}
    :component communities.overview/view}

   ;; Note: the sheet screen is used when selecting addresses to share when
   ;; joining a community. The non-sheet screen is used when editing shared
   ;; addresses after the join request was sent.
   {:name      :community-account-selection-sheet
    :options   {:sheet? true}
    :component communities.accounts-selection/view}
   {:name      :community-account-selection
    :options   {:insets {:top? true}}
    :component communities.accounts-selection/view}

   {:name      :community-requests-to-join
    :options   {:sheet? true}
    :component join-menu/view}

   {:name      :screen/share-community
    :options   options/transparent-screen-options
    :component share-community/view}

   {:name      :invite-people-community
    :options   {:sheet? true}
    :component communities.invite/view}

   {:name      :share-community-channel
    :options   options/transparent-screen-options
    :component share-community-channel/view}

   {:name      :screen/chat.view-channel-members-and-details
    :options   {:insets {:top? true}}
    :component channel-view-channel-members-and-details/view}

   {:name      :addresses-for-permissions
    :options   {:insets {:top? true}}
    :component addresses-for-permissions/view}

   {:name      :address-for-airdrop
    :options   {:insets {:top? true}}
    :component airdrop-addresses/view}
  ])

(defn contact-screens
  []
  [{:name      :new-contact
    :options   {:sheet? true}
    :component add-new-contact/new-contact}

   {:name      :scan-profile-qr-code
    :options   options/dark-screen
    :component scan-profile-qr-page/view}

   {:name      :contact-profile
    :options   {:modalPresentationStyle :overCurrentContext}
    :component contact-profile/view}

   {:name      :share-contact
    :options   options/transparent-screen-options
    :component share-contact/view}
  ])

(defn device-syncing-screens
  []
  [{:name      :how-to-pair
    :options   (assoc options/dark-screen :sheet? true)
    :component how-to-pair/view}

   {:name      :find-sync-code
    :options   (assoc options/dark-screen :sheet? true)
    :component find-sync-code/view}

   {:name      :screen/settings.syncing
    :options   options/transparent-modal-screen-options
    :component settings.syncing/view}

   {:name      :screen/paired-devices
    :options   options/transparent-modal-screen-options
    :component syncing-devices-list/view}

   {:name      :settings-setup-syncing
    :options   options/transparent-screen-options
    :component settings-setup-syncing/view}

   {:name      :scan-sync-code-page
    :options   options/transparent-modal-screen-options
    :component scan-sync-code-page/view}
  ])

(defn settings-screens
  []
  [{:name      :settings
    :options   options/transparent-screen-options
    :component settings/view}

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

   {:name      :screen/settings-password
    :options   options/transparent-modal-screen-options
    :component settings-password/view}

   {:name      :screen/change-password
    :options   (assoc options/transparent-modal-screen-options :theme :dark)
    :component change-password/view}

   {:name      :screen/change-password-loading
    :options   (assoc
                options/transparent-modal-screen-options
                :theme              :dark
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false})
    :component change-password-loading/view}

   {:name      :screen/settings-messages
    :options   options/transparent-modal-screen-options
    :component settings.messages/view}

   {:name      :screen/settings-blocked-users
    :options   options/transparent-modal-screen-options
    :component settings.blocked-users/view}

   {:name      :screen/settings-privacy-and-security
    :options   options/transparent-modal-screen-options
    :component settings.privacy-and-security/view}

   {:name      :screen/settings.share-usage-data
    :options   options/transparent-modal-screen-options
    :component settings.share-usage/view}

   {:name      :screen/settings.language-and-currency
    :options   options/transparent-modal-screen-options
    :component settings.language-and-currency/view}

   {:name      :screen/settings.currency-selection
    :options   options/transparent-modal-screen-options
    :component settings.currency-selection/view}
  ])

(defn wallet-settings-screens
  []
  [{:name      :screen/settings.wallet
    :options   options/transparent-modal-screen-options
    :component wallet-options/view}

   {:name      :screen/settings.rename-keypair
    :options   options/transparent-screen-options
    :component keypair-rename/view}

   {:name      :screen/settings.encrypted-keypair-qr
    :options   options/transparent-screen-options
    :component encrypted-keypair-qr/view}

   {:name      :screen/settings.saved-addresses
    :options   options/transparent-modal-screen-options
    :component saved-addresses-settings/view}

   {:name      :screen/settings.keypairs-and-accounts
    :options   options/transparent-modal-screen-options
    :component keypairs-and-accounts/view}

   {:name      :screen/settings.scan-keypair-qr
    :options   options/transparent-screen-options
    :component scan-keypair-qr/view}

   {:name      :screen/settings.missing-keypair.import-seed-phrase
    :options   options/transparent-screen-options
    :component missing-keypairs.import-seed-phrase/view}

   {:name      :screen/settings.missing-keypair-import-private-key
    :options   options/transparent-screen-options
    :component missing-keypairs.import-private-key/view}

   {:name      :screen/settings.network-settings
    :options   options/transparent-modal-screen-options
    :component network-settings/view}

   {:name      :screen/settings.save-address
    :options   options/transparent-modal-screen-options
    :component wallet-save-address/view}

   {:name      :screen/settings.edit-saved-address
    :options   (assoc options/dark-screen :sheet? true)
    :component wallet-save-address/view}

   {:name      :screen/settings.add-address-to-save
    :options   options/transparent-modal-screen-options
    :component wallet-add-address-to-save/view}

   {:name      :screen/settings.share-saved-address
    :options   options/transparent-screen-options
    :component share-saved-address/view}])

(defn wallet-screens
  []
  [{:name      :screen/wallet.accounts
    :options   {:insets {:top? true}}
    :component wallet-accounts/view}

   {:name      :screen/wallet.edit-account
    :component wallet-edit-account/view}

   {:name      :screen/wallet.add-address-to-watch
    :options   {:insets {:top? true}}
    :component wallet-add-address-to-watch/view}

   {:name      :screen/wallet.confirm-address-to-watch
    :component wallet-confirm-address-to-watch/view}

   {:name      :screen/wallet.bridge-select-asset
    :options   {:insets                 {:top? true}
                :modalPresentationStyle :overCurrentContext}
    :component wallet-bridge-select-asset/view}

   {:name      :screen/wallet.bridge-to
    :options   {:insets                 {:top? true}
                :modalPresentationStyle :overCurrentContext}
    :component wallet-bridge-to/view}

   {:name      :screen/wallet.bridge-input-amount
    :options   {:insets {:top? true}}
    :component wallet-bridge-input-amount/view}

   {:name      :screen/wallet.edit-derivation-path
    :component wallet-edit-derivation-path/view}

   {:name      :screen/wallet.import-private-key
    :options   {:insets {:top? true}}
    :component wallet-import-private-key/view}

   {:name      :screen/wallet.collectible
    :component wallet-collectible/view}

   {:name      :screen/wallet.select-keypair
    :options   {:insets {:top? true :bottom? true}}
    :component wallet-select-keypair/view}

   {:name      :screen/wallet.create-account
    :options   {:insets {:top? true}}
    :component wallet-create-account/view}

   {:name      :screen/wallet.backup-recovery-phrase
    :options   {:insets {:top? true :bottom? true}}
    :component wallet-backup-recovery-phrase/view}

   {:name      :screen/wallet.confirm-backup
    :options   {:insets {:top? true :bottom? true}}
    :component wallet-confirm-backup/view}

   {:name      :screen/wallet.keypair-name
    :options   {:insets {:top? true}}
    :component wallet-key-pair-name/view}

   {:name      :screen/wallet.enter-seed-phrase
    :component enter-seed-phrase/view}

   {:name      :screen/wallet.share-address
    :options   options/transparent-screen-options
    :component wallet-share-address/view}

   {:name      :screen/wallet.send-input-amount
    :options   {:modalPresentationStyle :overCurrentContext
                :insets                 {:top?    true
                                         :bottom? true}}
    :component wallet-send-input-amount/view}

   {:name      :screen/wallet.select-address
    :options   {:modalPresentationStyle :overCurrentContext
                :insets                 {:top? true}}
    :component wallet-select-address/view}

   {:name      :screen/wallet.select-from
    :options   {:modalPresentationStyle :overCurrentContext
                :insets                 {:top? true}}
    :component wallet-select-from/view}

   {:name      :screen/wallet.select-asset
    :options   {:insets {:top? true}}
    :component wallet-select-asset/view}

   {:name      :screen/wallet.transaction-confirmation
    :component wallet-transaction-confirmation/view}

   {:name      :screen/wallet.select-collectible-amount
    :options   {:insets {:top? true}}
    :component wallet-select-collectible-amount/view}

   {:name      :screen/wallet.transaction-progress
    :component wallet-transaction-progress/view}

   {:name      :screen/wallet.scan-address
    :options   options/dark-screen
    :component wallet-scan-address/view}

   {:name      :screen/wallet.swap-select-asset-to-pay
    :options   {:modalPresentationStyle :overCurrentContext
                :insets                 {:top? true}}
    :component wallet-swap-select-asset-to-pay/view}

   {:name      :screen/wallet.setup-swap
    :options   {:insets {:bottom? true}}
    :component wallet-swap-setup-swap/view}

   {:name      :screen/wallet.swap-propasal
    :options   {:insets {:top? true}}
    :component wallet-swap-propasal/view}

   {:name      :screen/wallet.swap-confirmation
    :options   {:modalPresentationStyle :overCurrentContext}
    :component wallet-swap-confirmation/view}

   {:name      :screen/wallet.swap-set-spending-cap
    :options   {:sheet? true}
    :component wallet-swap-set-spending-cap/view}])

(defn wallet-connect-screens
  []
  [{:name      :screen/wallet.wallet-connect-session-proposal
    :options   {:sheet? true}
    :component wallet-connect-session-proposal/view}

   {:name      :screen/wallet-connect.sign-message
    :options   {:sheet? true}
    :component wallet-connect-sign-message/view}

   {:name      :screen/wallet-connect.sign-transaction
    :options   {:sheet? true}
    :component wallet-connect-sign-transaction/view}

   {:name      :screen/wallet-connect.send-transaction
    :options   {:sheet? true}
    :component wallet-connect-send-transaction/view}

   {:name      :screen/wallet.connected-dapps
    :options   {:insets {:top? true}}
    :component wallet-connected-dapps/view}

   {:name      :screen/wallet.scan-dapp
    :options   options/dark-screen
    :component wallet-scan-dapp/view}])

(defn onboarding-screens
  []
  [{:name      :screen/onboarding.intro
    :options   {:theme :dark}
    :on-focus  [:onboarding/overlay-dismiss]
    :component intro/view}

   {:name      :screen/onboarding.new-to-status
    :options   {:theme                  :dark
                :layout                 options/onboarding-transparent-layout
                :animations             (merge
                                         transitions/new-to-status-modal-animations
                                         transitions/push-animations-for-transparent-background)
                :popGesture             false
                :modalPresentationStyle :overCurrentContext}
    :component create-or-sync-profile/create-profile}

   {:name      :screen/onboarding.sync-or-recover-profile
    :options   {:theme                  :dark
                :layout                 options/onboarding-transparent-layout
                :animations             (merge
                                         transitions/new-to-status-modal-animations
                                         transitions/push-animations-for-transparent-background)
                :popGesture             false
                :modalPresentationStyle :overCurrentContext}
    :component create-or-sync-profile/sync-or-recover-profile}

   {:name      :screen/onboarding.create-profile
    :options   {:theme      :dark
                :layout     options/onboarding-transparent-layout
                :animations transitions/push-animations-for-transparent-background
                :popGesture false}
    :component create-profile/create-profile}

   {:name      :screen/onboarding.create-profile-password
    :options   {:theme      :dark
                :insets     {:top false}
                :layout     options/onboarding-transparent-layout
                :animations transitions/push-animations-for-transparent-background
                :popGesture false}
    :component create-password/create-password}

   {:name      :screen/onboarding.enable-biometrics
    :options   {:theme                  :dark
                :layout                 options/onboarding-transparent-layout
                :animations             (merge
                                         transitions/new-to-status-modal-animations
                                         transitions/push-animations-for-transparent-background)
                :popGesture             false
                :modalPresentationStyle :overCurrentContext
                :hardwareBackButton     {:dismissModalOnPress false
                                         :popStackOnPress     false}}
    :component enable-biometrics/view}

   {:name      :screen/onboarding.generating-keys
    :options   {:theme              :dark
                :layout             options/onboarding-transparent-layout
                :animations         transitions/push-animations-for-transparent-background
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :component generating-keys/view}

   {:name      :screen/onboarding.preparing-status
    :options   {:theme              :dark
                :layout             options/onboarding-transparent-layout
                :animations         transitions/push-animations-for-transparent-background
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :component preparing-status/view}

   {:name      :screen/onboarding.enter-seed-phrase
    :options   {:theme      :dark
                :layout     options/onboarding-transparent-layout
                :animations transitions/push-animations-for-transparent-background
                :popGesture false}
    :component enter-seed-phrase/view}

   {:name      :screen/onboarding.enable-notifications
    :options   {:theme                  :dark
                :layout                 options/onboarding-transparent-layout
                :animations             transitions/push-animations-for-transparent-background
                :popGesture             false
                :modalPresentationStyle :overCurrentContext
                :hardwareBackButton     {:dismissModalOnPress false
                                         :popStackOnPress     false}}
    :component enable-notifications/view}

   {:name      :screen/onboarding.identifiers
    :component identifiers/view
    :options   {:theme              :dark
                :layout             options/onboarding-transparent-layout
                :animations         transitions/push-animations-for-transparent-background
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}}

   {:name      :screen/onboarding.sign-in-intro
    :options   {:layout                 options/onboarding-transparent-layout
                :animations             (merge
                                         transitions/sign-in-modal-animations
                                         transitions/push-animations-for-transparent-background)
                :modalPresentationStyle :overCurrentContext}
    :component sign-in/animated-view}

   {:name      :screen/onboarding.sign-in
    :options   {:theme                  :dark
                :modalPresentationStyle :overCurrentContext
                :layout                 options/onboarding-layout}
    :component sign-in/view}

   {:name      :screen/onboarding.syncing-progress
    :options   (assoc options/dark-screen
                      :popGesture
                      false)
    :component syncing-devices/view}

   {:name      :screen/onboarding.syncing-progress-intro
    :options   {:theme      :dark
                :layout     options/onboarding-transparent-layout
                :animations transitions/push-animations-for-transparent-background
                :popGesture false}
    :component syncing-devices/view-onboarding}

   {:name      :screen/onboarding.syncing-results
    :options   {:theme :dark}
    :component syncing-results/view}])

(defn screens
  []
  (concat
   (old-screens/screens)
   (chat-screens)
   (community-screens)
   (contact-screens)
   (device-syncing-screens)
   (settings-screens)
   (wallet-settings-screens)
   (wallet-screens)
   (wallet-connect-screens)
   (onboarding-screens)

   [{:name      :activity-center
     :options   options/transparent-screen-options
     :component activity-center/view}

    {:name      :screen/share-shell
     :options   options/transparent-screen-options
     :component share/view}

    {:name      :shell-stack
     :component shell/shell-stack}

    {:name      :shell-qr-reader
     :options   options/dark-screen
     :component shell-qr-reader/view}

    {:name      :lightbox
     :options   options/lightbox
     :component lightbox/lightbox}

    {:name      :photo-selector
     :options   {:sheet? true}
     :component photo-selector/photo-selector}

    {:name      :camera-screen
     :options   {:navigationBar {:backgroundColor colors/black}
                 :theme         :dark}
     :component camera-screen/camera-screen}

    {:name      :emoji-picker
     :options   {:sheet? true}
     :component emoji-picker/view}

    {:name      :screen/profile.profiles
     :options   {:theme  :dark
                 :layout options/onboarding-layout}
     :on-focus  [:onboarding/overlay-dismiss]
     :component profiles/view}]

   [{:name    :shell
     :options {:theme :dark}}]

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
