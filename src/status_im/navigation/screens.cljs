(ns status-im.navigation.screens
  (:require
    [legacy.status-im.ui.screens.screens :as old-screens]
    [quo.foundations.colors :as colors]
    [status-im.common.emoji-picker.view :as emoji-picker]
    [status-im.common.enter-seed-phrase.view :as enter-seed-phrase]
    [status-im.common.lightbox.view :as lightbox]
    [status-im.common.pdf-viewer.view :as pdf-viewer]
    [status-im.config :as config]
    [status-im.contexts.chat.group.create.view :as group.create]
    [status-im.contexts.chat.group.details.view :as group.details]
    [status-im.contexts.chat.group.update.view :as group.update]
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
    [status-im.contexts.keycard.authorise.view :as keycard.authorise]
    [status-im.contexts.keycard.check.view :as keycard.check]
    [status-im.contexts.keycard.empty.view :as keycard.empty]
    [status-im.contexts.keycard.error.view :as keycard.error]
    [status-im.contexts.keycard.not-keycard.view :as keycard.not-keycard]
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
    [status-im.contexts.profile.backup-recovery-phrase.view :as backup-recovery-phrase]
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
    [status-im.contexts.settings.keycard.view :as settings.keycard]
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
    [status-im.navigation.transitions :as transitions]
    [utils.collection]))

(def chat-screens
  [{:name      :start-a-new-chat
    :metrics   {:track?   :true
                :alias-id :messenger.new-chat}
    :options   {:sheet? true}
    :component new-chat/view}

   {:name      :chat
    :metrics   {:track?   :true
                :alias-id :messenger.chat}
    :options   {:popGesture false
                :animations transitions/stack-transition-from-bottom}
    :component chat/chat}

   {:name      :screen/group-create
    :metrics   {:track?   :true
                :alias-id :messenger.new-group}
    :options   {:sheet?           true
                :skip-background? true}
    :component group.create/view}

   {:name      :screen/group-details
    :metrics   {:track?   :true
                :alias-id :messenger.group-details}
    :component group.details/view}

   {:name      :screen/group-update
    :metrics   {:track?   :true
                :alias-id :messenger.group-update}
    :options   {:sheet?           true
                :skip-background? true}
    :component group.update/view}

   {:name      :screen/group-add-manage-members
    :metrics   {:track?   :true
                :flow     :messenger
                :alias-id :messenger.group-manage-members}
    :options   {:sheet? true}
    :component group.details/add-manage-members}])

(def community-screens
  [{:name      :discover-communities
    :metrics   {:track?   :true
                :alias-id :community.discover}
    :component communities.discover/view}

   {:name      :community-overview
    :metrics   {:track?   :true
                :alias-id :community.overview}
    :options   {:animations transitions/stack-transition-from-bottom}
    :component communities.overview/view}

   ;; Note: the sheet screen is used when selecting addresses to share when
   ;; joining a community. The non-sheet screen is used when editing shared
   ;; addresses after the join request was sent.
   {:name      :community-account-selection-sheet
    :metrics   {:track?   :true
                :alias-id :community.select-addresses-for-joining-community}
    :options   {:sheet? true}
    :component communities.accounts-selection/view}
   {:name      :community-account-selection
    :metrics   {:track?   :true
                :alias-id :community.select-addresses-for-community}
    :options   {:insets {:top? true}}
    :component communities.accounts-selection/view}

   {:name      :community-requests-to-join
    :metrics   {:track?   :true
                :alias-id :community.request-to-join}
    :options   {:sheet? true}
    :component join-menu/view}

   {:name      :screen/share-community
    :metrics   {:track?   :true
                :alias-id :community.share-community}
    :options   options/transparent-screen-options
    :component share-community/view}

   {:name      :invite-people-community
    :metrics   {:track?   :true
                :alias-id :community.invite-people}
    :options   {:sheet? true}
    :component communities.invite/view}

   {:name      :share-community-channel
    :metrics   {:track?   :true
                :alias-id :community.share-channel}
    :options   options/transparent-screen-options
    :component share-community-channel/view}

   {:name      :screen/chat.view-channel-members-and-details
    :metrics   {:track?   :true
                :alias-id :community.view-channel-members-and-details}
    :options   {:insets {:top? true}}
    :component channel-view-channel-members-and-details/view}

   {:name      :addresses-for-permissions
    :metrics   {:track?   :true
                :alias-id :community.choose-addresses-for-permissions}
    :options   {:insets {:top? true}}
    :component addresses-for-permissions/view}

   {:name      :address-for-airdrop
    :metrics   {:track?   :true
                :alias-id :community.choose-addresses-for-airdrop}
    :options   {:insets {:top? true}}
    :component airdrop-addresses/view}])

(def contact-screens
  [{:name      :new-contact
    :metrics   {:track?   :true
                :alias-id :contact.new-contact}
    :options   {:sheet? true}
    :component add-new-contact/new-contact}

   {:name      :scan-profile-qr-code
    :metrics   {:track?   :true
                :alias-id :contact.scan-profile-qr-code}
    :options   options/dark-screen
    :component scan-profile-qr-page/view}

   {:name      :contact-profile
    :metrics   {:track?   :true
                :alias-id :contact.contact-profile}
    :options   {:modalPresentationStyle :overCurrentContext}
    :component contact-profile/view}

   {:name      :share-contact
    :metrics   {:track?   :true
                :alias-id :contact.share-profile}
    :options   options/transparent-screen-options
    :component share-contact/view}])

(def device-syncing-screens
  [{:name      :how-to-pair
    :metrics   {:track?   true
                :alias-id :syncing.how-to-pair}
    :options   (assoc options/dark-screen :sheet? true)
    :component how-to-pair/view}

   {:name      :find-sync-code
    :metrics   {:track?   true
                :alias-id :syncing.find-sync-code}
    :options   (assoc options/dark-screen :sheet? true)
    :component find-sync-code/view}

   {:name      :settings-setup-syncing
    :metrics   {:track?   true
                :alias-id :syncing.setup-syncing}
    :options   options/transparent-screen-options
    :component settings-setup-syncing/view}

   {:name      :scan-sync-code-page
    :metrics   {:track?   true
                :alias-id :syncing.scan-sync-code}
    :options   options/transparent-modal-screen-options
    :component scan-sync-code-page/view}])

(def settings-screens
  [{:name      :settings
    :metrics   {:track?   :true
                :alias-id :settings.profile-settings}
    :options   options/transparent-screen-options
    :component settings/view}

   {:name      :screen/settings.keycard
    :metrics   {:track? :true}
    :options   options/keycard-modal-screen-options
    :component settings.keycard/view}

   {:name      :edit-profile
    :metrics   {:track?   :true
                :alias-id :settings.edit-profile}
    :options   options/transparent-modal-screen-options
    :component edit-profile/view}

   {:name      :edit-accent-colour
    :metrics   {:track?   :true
                :alias-id :settings.edit-profile-accent-colour}
    :options   options/transparent-modal-screen-options
    :component edit-accent-colour/view}

   {:name      :edit-name
    :metrics   {:track?   :true
                :alias-id :settings.edit-profile-name}
    :options   options/transparent-modal-screen-options
    :component edit-name/view}

   {:name      :edit-bio
    :metrics   {:track?   :true
                :alias-id :settings.edit-profile-bio}
    :options   options/transparent-modal-screen-options
    :component edit-bio/view}

   {:name      :screen/settings-password
    :metrics   {:track?   :true
                :alias-id :settings.password}
    :options   options/transparent-modal-screen-options
    :component settings-password/view}

   {:name      :screen/change-password
    :metrics   {:track?   :true
                :alias-id :settings.change-password}
    :options   (assoc options/transparent-modal-screen-options :theme :dark)
    :component change-password/view}

   {:name      :screen/change-password-loading
    :metrics   {:track?   :true
                :alias-id :settings.change-password-loading}
    :options   (assoc
                options/transparent-modal-screen-options
                :theme              :dark
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false})
    :component change-password-loading/view}

   {:name      :screen/settings-messages
    :metrics   {:track?   :true
                :alias-id :settings.messages}
    :options   options/transparent-modal-screen-options
    :component settings.messages/view}

   {:name      :screen/settings-blocked-users
    :metrics   {:track?   :true
                :alias-id :settings.blocked-users}
    :options   options/transparent-modal-screen-options
    :component settings.blocked-users/view}

   {:name      :screen/settings-privacy-and-security
    :metrics   {:track?   :true
                :alias-id :settings.private-and-security}
    :options   options/transparent-modal-screen-options
    :component settings.privacy-and-security/view}

   {:name      :screen/settings.share-usage-data
    :metrics   {:track? :true}
    :options   options/transparent-modal-screen-options
    :component settings.share-usage/view}

   {:name      :screen/settings.syncing
    :metrics   {:track? true}
    :options   options/transparent-modal-screen-options
    :component settings.syncing/view}

   {:name      :screen/paired-devices
    :metrics   {:track?   true
                :alias-id :settings.paired-devices}
    :options   options/transparent-modal-screen-options
    :component syncing-devices-list/view}

   {:name      :screen/settings.language-and-currency
    :metrics   {:track? :true}
    :options   options/transparent-modal-screen-options
    :component settings.language-and-currency/view}

   {:name      :screen/settings.currency-selection
    :metrics   {:track? :true}
    :options   options/transparent-modal-screen-options
    :component settings.currency-selection/view}])

(def wallet-settings-screens
  [{:name      :screen/settings.wallet
    :metrics   {:track? :true}
    :options   options/transparent-modal-screen-options
    :component wallet-options/view}

   {:name      :screen/settings.rename-keypair
    :metrics   {:track?   :true
                :alias-id :settings.wallet-rename-keypair}
    :options   options/transparent-screen-options
    :component keypair-rename/view}

   {:name      :screen/settings.encrypted-keypair-qr
    :metrics   {:track?   :true
                :alias-id :settings.wallet-encrypted-keypair-qr}
    :options   options/transparent-screen-options
    :component encrypted-keypair-qr/view}

   {:name      :screen/settings.saved-addresses
    :metrics   {:track?   :true
                :alias-id :settings.wallet-saved-addresses}
    :options   options/transparent-modal-screen-options
    :component saved-addresses-settings/view}

   {:name      :screen/settings.keypairs-and-accounts
    :metrics   {:track?   :true
                :alias-id :settings.wallet-keypairs-and-accounts}
    :options   options/transparent-modal-screen-options
    :component keypairs-and-accounts/view}

   {:name      :screen/settings.scan-keypair-qr
    :metrics   {:track?   :true
                :alias-id :settings.wallet-scan-keypair-qr}
    :options   options/transparent-screen-options
    :component scan-keypair-qr/view}

   {:name      :screen/settings.missing-keypair.import-seed-phrase
    :metrics   {:track?   :true
                :alias-id :settings.wallet-missing-keypair-import-seed-phrase}
    :options   options/transparent-screen-options
    :component missing-keypairs.import-seed-phrase/view}

   {:name      :screen/settings.missing-keypair-import-private-key
    :metrics   {:track?   :true
                :alias-id :settings.wallet-missing-keypair-import-private-key}
    :options   options/transparent-screen-options
    :component missing-keypairs.import-private-key/view}

   {:name      :screen/settings.network-settings
    :metrics   {:track?   :true
                :alias-id :settings.wallet-network-settings}
    :options   options/transparent-modal-screen-options
    :component network-settings/view}

   {:name      :screen/settings.save-address
    :metrics   {:track?   :true
                :alias-id :settings.wallet-saved-addresses}
    :options   options/transparent-modal-screen-options
    :component wallet-save-address/view}

   {:name      :screen/settings.edit-saved-address
    :metrics   {:track?   :true
                :alias-id :settings.wallet-edit-saved-addresses}
    :options   (assoc options/dark-screen :sheet? true)
    :component wallet-save-address/view}

   {:name      :screen/settings.add-address-to-save
    :metrics   {:track?   :true
                :alias-id :settings.wallet-add-saved-address}
    :options   options/transparent-modal-screen-options
    :component wallet-add-address-to-save/view}

   {:name      :screen/settings.share-saved-address
    :metrics   {:track?   :true
                :alias-id :settings.wallet-share-saved-address}
    :options   options/transparent-screen-options
    :component share-saved-address/view}])

(def wallet-screens
  [{:name      :screen/wallet.accounts
    :metrics   {:track?   true
                :alias-id :wallet.account}
    :options   {:insets {:top? true}}
    :component wallet-accounts/view}

   {:name      :screen/wallet.collectible
    :metrics   {:track? true}
    :component wallet-collectible/view}

   {:name      :screen/wallet.edit-account
    :metrics   {:track? true}
    :component wallet-edit-account/view}

   {:name      :screen/wallet.create-account
    :metrics   {:track?   true
                :alias-id :wallet.create-account}
    :options   {:insets {:top? true}}
    :component wallet-create-account/view}

   {:name      :screen/wallet.select-keypair
    :metrics   {:track?   true
                :alias-id :wallet.create-account-select-keypair}
    :options   {:insets {:top? true :bottom? true}}
    :component wallet-select-keypair/view}

   {:name      :screen/wallet.edit-derivation-path
    :metrics   {:track?   true
                :alias-id :wallet.create-account-edit-derivation-path}
    :component wallet-edit-derivation-path/view}

   {:name      :screen/wallet.confirm-backup
    :metrics   {:track?   true
                :alias-id :wallet.create-account-backup-new-keypair-confirm}
    :options   {:insets {:top? true :bottom? true}}
    :component wallet-confirm-backup/view}

   {:name      :screen/wallet.keypair-name
    :metrics   {:track?   true
                :alias-id :wallet.create-account-add-new-keypair-name}
    :options   {:insets {:top? true}}
    :component wallet-key-pair-name/view}

   {:name      :screen/wallet.import-private-key
    :metrics   {:track?   true
                :alias-id :wallet.create-account-import-keypair-by-private-key}
    :options   {:insets {:top? true}}
    :component wallet-import-private-key/view}

   {:name      :screen/wallet.share-address
    :metrics   {:track? true}
    :options   options/transparent-screen-options
    :component wallet-share-address/view}

   {:name      :screen/wallet.add-address-to-watch
    :metrics   {:track?   true
                :alias-id :wallet.add-watched-address}
    :options   {:insets {:top? true}}
    :component wallet-add-address-to-watch/view}

   {:name      :screen/wallet.confirm-address-to-watch
    :metrics   {:track?   true
                :alias-id :wallet.add-watched-address-profile}
    :component wallet-confirm-address-to-watch/view}

   {:name      :screen/wallet.transaction-confirmation
    :metrics   {:track? true}
    :component wallet-transaction-confirmation/view}

   {:name      :screen/wallet.transaction-progress
    :metrics   {:track? true}
    :component wallet-transaction-progress/view}])

(def wallet-send-screens
  [{:name      :screen/wallet.scan-address
    :metrics   {:track?   true
                :alias-id :wallet-send.scan-address}
    :options   options/dark-screen
    :component wallet-scan-address/view}

   {:name      :screen/wallet.select-address
    :metrics   {:track?   true
                :alias-id :wallet-send.select-destination-address}
    :options   {:modalPresentationStyle :overCurrentContext
                :insets                 {:top? true}}
    :component wallet-select-address/view}

   {:name      :screen/wallet.select-from
    :metrics   {:track?   true
                :alias-id :wallet-send.select-source-address}
    :options   {:modalPresentationStyle :overCurrentContext
                :insets                 {:top? true}}
    :component wallet-select-from/view}

   {:name      :screen/wallet.select-asset
    :metrics   {:track?   true
                :alias-id :wallet-send.select-asset}
    :options   {:insets {:top? true}}
    :component wallet-select-asset/view}

   {:name      :screen/wallet.send-input-amount
    :metrics   {:track?   true
                :alias-id :wallet-send.input-amount}
    :options   {:modalPresentationStyle :overCurrentContext
                :insets                 {:top?    true
                                         :bottom? true}}
    :component wallet-send-input-amount/view}

   {:name      :screen/wallet.select-collectible-amount
    :metrics   {:track?   true
                :alias-id :wallet-send.select-collectible-amount}
    :options   {:insets {:top? true}}
    :component wallet-select-collectible-amount/view}])

(def wallet-bridge-screens
  [{:name      :screen/wallet.bridge-select-asset
    :metrics   {:track?   true
                :alias-id :wallet-bridge.select-asset}
    :options   {:insets                 {:top? true}
                :modalPresentationStyle :overCurrentContext}
    :component wallet-bridge-select-asset/view}

   {:name      :screen/wallet.bridge-to
    :metrics   {:track?   true
                :alias-id :wallet-bridge.select-network}
    :options   {:insets                 {:top? true}
                :modalPresentationStyle :overCurrentContext}
    :component wallet-bridge-to/view}

   {:name      :screen/wallet.bridge-input-amount
    :metrics   {:track?   true
                :alias-id :wallet-bridge.input-amount-to-bridge}
    :options   {:insets {:top? true}}
    :component wallet-bridge-input-amount/view}])

(def wallet-swap-screens
  [{:name      :screen/wallet.swap-select-asset-to-pay
    :metrics   {:track?   true
                :alias-id :wallet-swap.select-asset-to-pay}
    :options   {:modalPresentationStyle :overCurrentContext
                :insets                 {:top? true}}
    :component wallet-swap-select-asset-to-pay/view}

   {:name      :screen/wallet.setup-swap
    :metrics   {:track?   true
                :alias-id :wallet-swap.input-amount-to-swap}
    :options   {:modalPresentationStyle :overCurrentContext
                :insets                 {:bottom? true}}
    :component wallet-swap-setup-swap/view}

   {:name      :screen/wallet.swap-propasal
    :metrics   {:track?   true
                :alias-id :wallet-swap.swap-proposal}
    :options   {:insets {:top? true}}
    :component wallet-swap-propasal/view}

   {:name      :screen/wallet.swap-set-spending-cap
    :metrics   {:track?   true
                :alias-id :wallet-swap.set-spending-cap}
    :options   {:sheet? true}
    :component wallet-swap-set-spending-cap/view}

   {:name      :screen/wallet.swap-confirmation
    :metrics   {:track?   true
                :alias-id :wallet-swap.swap-confirmation}
    :options   {:modalPresentationStyle :overCurrentContext}
    :component wallet-swap-confirmation/view}])

(def wallet-connect-screens
  [{:name      :screen/wallet.wallet-connect-session-proposal
    :metrics   {:track?   true
                :alias-id :wallet-connect.session-proposal}
    :options   {:sheet? true}
    :component wallet-connect-session-proposal/view}

   {:name      :screen/wallet-connect.sign-message
    :metrics   {:track? true}
    :options   {:sheet? true}
    :component wallet-connect-sign-message/view}

   {:name      :screen/wallet-connect.sign-transaction
    :metrics   {:track? true}
    :options   {:sheet? true}
    :component wallet-connect-sign-transaction/view}

   {:name      :screen/wallet-connect.send-transaction
    :metrics   {:track? true}
    :options   {:sheet? true}
    :component wallet-connect-send-transaction/view}

   {:name      :screen/wallet.connected-dapps
    :metrics   {:track?   true
                :alias-id :wallet-connect.connected-dapps}
    :options   {:insets {:top? true}}
    :component wallet-connected-dapps/view}

   {:name      :screen/wallet.scan-dapp
    :metrics   {:track?   true
                :alias-id :wallet-connect.scan-dapp}
    :options   options/dark-screen
    :component wallet-scan-dapp/view}])

(def onboarding-intro
  {:name      :screen/onboarding.intro
   :metrics   {:track? true}
   :options   {:theme :dark}
   :on-focus  [:onboarding/overlay-dismiss]
   :component intro/view})

(def onboarding-new-to-status
  {:name      :screen/onboarding.new-to-status
   :metrics   {:track?   true
               :alias-id :onboarding.create-profile-intro}
   :options   {:theme                  :dark
               :layout                 options/onboarding-transparent-layout
               :animations             (merge
                                        transitions/new-to-status-modal-animations
                                        transitions/push-animations-for-transparent-background)
               :popGesture             false
               :modalPresentationStyle :overCurrentContext}
   :component create-or-sync-profile/create-profile})

(def onboarding-sync-or-recover-profile
  {:name      :screen/onboarding.sync-or-recover-profile
   :metrics   {:track? true}
   :options   {:theme                  :dark
               :layout                 options/onboarding-transparent-layout
               :animations             (merge
                                        transitions/new-to-status-modal-animations
                                        transitions/push-animations-for-transparent-background)
               :popGesture             false
               :modalPresentationStyle :overCurrentContext}
   :component create-or-sync-profile/sync-or-recover-profile})

(def onboarding-create-profile
  {:name      :screen/onboarding.create-profile
   :metrics   {:track?   true
               :alias-id :onboarding.create-profile-info}
   :options   {:theme      :dark
               :layout     options/onboarding-transparent-layout
               :animations transitions/push-animations-for-transparent-background
               :popGesture false}
   :component create-profile/create-profile})

(def onboarding-create-profile-password
  {:name      :screen/onboarding.create-profile-password
   :metrics   {:track?   true
               :alias-id :onboarding.create-profile-password}
   :options   {:theme      :dark
               :insets     {:top false}
               :layout     options/onboarding-transparent-layout
               :animations transitions/push-animations-for-transparent-background
               :popGesture false}
   :component create-password/create-password})

(def onboarding-enable-biometrics
  {:name      :screen/onboarding.enable-biometrics
   :metrics   {:track? true}
   :options   {:theme                  :dark
               :layout                 options/onboarding-transparent-layout
               :animations             (merge
                                        transitions/new-to-status-modal-animations
                                        transitions/push-animations-for-transparent-background)
               :popGesture             false
               :modalPresentationStyle :overCurrentContext
               :hardwareBackButton     {:dismissModalOnPress false
                                        :popStackOnPress     false}}
   :component enable-biometrics/view})

(def onboarding-generating-keys
  {:name      :screen/onboarding.generating-keys
   :metrics   {:track? true}
   :options   {:theme              :dark
               :layout             options/onboarding-transparent-layout
               :animations         transitions/push-animations-for-transparent-background
               :popGesture         false
               :hardwareBackButton {:dismissModalOnPress false
                                    :popStackOnPress     false}}
   :component generating-keys/view})

(def onboarding-preparing-status
  {:name      :screen/onboarding.preparing-status
   :metrics   {:track? true}
   :options   {:theme              :dark
               :layout             options/onboarding-transparent-layout
               :animations         transitions/push-animations-for-transparent-background
               :popGesture         false
               :hardwareBackButton {:dismissModalOnPress false
                                    :popStackOnPress     false}}
   :component preparing-status/view})

(def onboarding-entering-seed-phrase
  {:name      :screen/onboarding.enter-seed-phrase
   :metrics   {:track?   true
               :alias-id :onboarding.sign-in-by-seed-phrase}
   :options   {:theme      :dark
               :layout     options/onboarding-transparent-layout
               :animations transitions/push-animations-for-transparent-background
               :popGesture false}
   :component enter-seed-phrase/view})

(def onboarding-enable-notifications
  {:name      :screen/onboarding.enable-notifications
   :metrics   {:track? true}
   :options   {:theme                  :dark
               :layout                 options/onboarding-transparent-layout
               :animations             transitions/push-animations-for-transparent-background
               :popGesture             false
               :modalPresentationStyle :overCurrentContext
               :hardwareBackButton     {:dismissModalOnPress false
                                        :popStackOnPress     false}}
   :component enable-notifications/view})

(def onboarding-identifiers
  {:name      :screen/onboarding.identifiers
   :metrics   {:track? true}
   :component identifiers/view
   :options   {:theme              :dark
               :layout             options/onboarding-transparent-layout
               :animations         transitions/push-animations-for-transparent-background
               :popGesture         false
               :hardwareBackButton {:dismissModalOnPress false
                                    :popStackOnPress     false}}})

(def onboarding-sign-in-intro
  {:name      :screen/onboarding.sign-in-intro
   :metrics   {:track?   true
               :alias-id :onboarding.sign-in-by-syncing}
   :options   {:layout                 options/onboarding-transparent-layout
               :animations             (merge
                                        transitions/sign-in-modal-animations
                                        transitions/push-animations-for-transparent-background)
               :modalPresentationStyle :overCurrentContext}
   :component sign-in/animated-view})

;; TODO(@seanstrom): Remove this definition if it is no longer needed
(def onboarding-sign-in
  {:name      :screen/onboarding.sign-in
   :metrics   {:track? true}
   :options   {:theme                  :dark
               :modalPresentationStyle :overCurrentContext
               :layout                 options/onboarding-layout}
   :component sign-in/view})

;; NOTE(@seanstrom):
;; This screen seems to be accessible from settings when syncing.
;; Should we consider this an onboarding screen?
(def onboarding-syncing-progress
  {:name      :screen/onboarding.syncing-progress
   :metrics   {:track?   true
               :alias-id :onboarding.syncing-devices}
   :options   (assoc options/dark-screen
                     :popGesture
                     false)
   :component syncing-devices/view})

(def onboarding-syncing-progress-intro
  {:name      :screen/onboarding.syncing-progress-intro
   :metrics   {:track?   true
               :alias-id :onboarding.sign-in-by-syncing-started}
   :options   {:theme      :dark
               :layout     options/onboarding-transparent-layout
               :animations transitions/push-animations-for-transparent-background
               :popGesture false}
   :component syncing-devices/view-onboarding})

(def onboarding-syncing-results
  {:name      :screen/onboarding.syncing-results
   :metrics   {:track?   true
               :alias-id :onboarding.syncing-completed}
   :options   {:theme :dark}
   :component syncing-results/view})

(def onboarding-screens
  [onboarding-intro
   onboarding-new-to-status
   onboarding-sync-or-recover-profile
   onboarding-create-profile
   onboarding-create-profile-password
   onboarding-enable-biometrics
   onboarding-generating-keys
   onboarding-preparing-status
   onboarding-entering-seed-phrase
   onboarding-enable-notifications
   onboarding-identifiers
   onboarding-sign-in-intro
   onboarding-sign-in
   onboarding-syncing-progress
   onboarding-syncing-progress-intro
   onboarding-syncing-results])

(def keycard-screens
  [{:name      :screen/keycard.check
    :metrics   {:track? true}
    :options   options/keycard-modal-screen-options
    :component keycard.check/view}

   {:name      :screen/keycard.empty
    :metrics   {:track? true}
    :options   options/keycard-modal-screen-options
    :component keycard.empty/view}

   {:name      :screen/keycard.error
    :metrics   {:track? true}
    :options   options/keycard-modal-screen-options
    :component keycard.error/view}

   {:name      :screen/keycard.not-keycard
    :metrics   {:track? true}
    :options   options/keycard-modal-screen-options
    :component keycard.not-keycard/view}

   {:name      :screen/keycard.authorise
    :metrics   {:track? true}
    :options   options/keycard-modal-screen-options
    :component keycard.authorise/view}])

(defn screens
  []
  (concat
   (old-screens/screens)
   chat-screens
   community-screens
   contact-screens
   device-syncing-screens
   settings-screens
   wallet-settings-screens
   wallet-screens
   wallet-send-screens
   wallet-bridge-screens
   wallet-swap-screens
   wallet-connect-screens
   onboarding-screens
   keycard-screens

   [{:name      :activity-center
     :metrics   {:track? true}
     :options   options/transparent-screen-options
     :component activity-center/view}

    {:name      :screen/share-shell
     :metrics   {:track? true}
     :options   options/transparent-screen-options
     :component share/view}

    {:name      :shell-stack
     :metrics   {:track? true}
     :component shell/shell-stack}

    {:name      :shell-qr-reader
     :metrics   {:track? true}
     :options   options/dark-screen
     :component shell-qr-reader/view}

    {:name      :lightbox
     :metrics   {:track? true}
     :options   options/lightbox
     :component lightbox/lightbox}

    {:name      :photo-selector
     :metrics   {:track? true}
     :options   {:sheet? true}
     :component photo-selector/photo-selector}

    {:name      :camera-screen
     :metrics   {:track? true}
     :options   {:navigationBar {:backgroundColor colors/black}
                 :theme         :dark}
     :component camera-screen/camera-screen}

    {:name      :emoji-picker
     :metrics   {:track? true}
     :options   {:sheet? true}
     :component emoji-picker/view}

    {:name      :screen/pdf-viewer
     :metrics   {:track? true}
     :options   {:insets                 {:top? true}
                 :modalPresentationStyle :overCurrentContext}
     :component pdf-viewer/view}

    {:name      :screen/backup-recovery-phrase
     :metrics   {:track? true}
     :options   {:insets {:top? true :bottom? true}}
     :component backup-recovery-phrase/view}

    {:name      :screen/use-recovery-phrase
     :metrics   {:track? true}
     :component enter-seed-phrase/view}

    {:name      :screen/profile.profiles
     :metrics   {:track?   true
                 :alias-id :app.profiles}
     :options   {:theme  :dark
                 :layout options/onboarding-layout}
     :on-focus  [:onboarding/overlay-dismiss]
     :component profiles/view}]

   [{:name    :shell
     :metrics {:track? true}
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

(def screens-by-name
  (utils.collection/index-by :name (screens)))
