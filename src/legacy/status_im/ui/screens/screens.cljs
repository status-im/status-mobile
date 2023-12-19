(ns legacy.status-im.ui.screens.screens
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.icons.icons :as icons]
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
    [legacy.status-im.ui.screens.keycard.authentication-method.views :as keycard.authentication]
    [legacy.status-im.ui.screens.keycard.onboarding.views :as keycard.onboarding]
    [legacy.status-im.ui.screens.keycard.pairing.views :as keycard.pairing]
    [legacy.status-im.ui.screens.keycard.pin.views :as keycard.pin]
    [legacy.status-im.ui.screens.keycard.recovery.views :as keycard.recovery]
    [legacy.status-im.ui.screens.keycard.settings.views :as keycard.settings]
    [legacy.status-im.ui.screens.keycard.views :as keycard]
    [legacy.status-im.ui.screens.link-previews-settings.views :as link-previews-settings]
    [legacy.status-im.ui.screens.log-level-settings.views :as log-level-settings]
    [legacy.status-im.ui.screens.mobile-network-settings.view :as mobile-network-settings]
    [legacy.status-im.ui.screens.network-info.views :as network-info]
    [legacy.status-im.ui.screens.network.edit-network.views :as edit-network]
    [legacy.status-im.ui.screens.network.network-details.views :as network-details]
    [legacy.status-im.ui.screens.network.views :as network]
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
    [legacy.status-im.ui.screens.wallet.account-settings.views :as account-settings]
    [legacy.status-im.ui.screens.wallet.account.views :as wallet.account]
    [legacy.status-im.ui.screens.wallet.accounts-manage.views :as accounts-manage]
    [legacy.status-im.ui.screens.wallet.accounts.views :as wallet.accounts]
    [legacy.status-im.ui.screens.wallet.add-new.views :as add-account]
    [legacy.status-im.ui.screens.wallet.buy-crypto.views :as wallet.buy-crypto]
    [legacy.status-im.ui.screens.wallet.collectibles.views :as wallet.collectibles]
    [legacy.status-im.ui.screens.wallet.custom-tokens.views :as custom-tokens]
    [legacy.status-im.ui.screens.wallet.recipient.views :as recipient]
    [legacy.status-im.ui.screens.wallet.send.views :as wallet.send]
    [legacy.status-im.ui.screens.wallet.settings.views :as wallet-settings]
    [legacy.status-im.ui.screens.wallet.swap.views :as wallet.swap]
    [legacy.status-im.ui.screens.wallet.transactions.views :as wallet-transactions]
    [status-im2.contexts.chat.group-details.view :as group-details]
    [utils.i18n :as i18n]))

(defn right-button-options
  [id icon]
  {:id   id
   :icon (icons/icon-source icon)})

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
                :topBar {:title {:text (i18n/label :t/sticker-market)}}}
    :component stickers/packs}

   {:name      :stickers-pack
    :options   {:insets {:top? true}}
    :component stickers/pack}

   {:name      :new-group
    :options   {:insets {:top? true}}
    ;;TODO custom subtitle
    :component group-chat/new-group}

   ;;WALLET

   {:name      :wallet-legacy
    :on-focus  [:wallet-legacy/tab-opened]
    ;;TODO wallet redesign
    ;;:options   {:statusBar {:backgroundColor quo.colors/neutral-5}}
    :component wallet.accounts/accounts-overview-old}
   {:name      :wallet-account
    ;;TODO dynamic titleaccounts-overview
    :options   {:insets {:top? true}}
    :component wallet.account/account}
   {:name      :add-new-account
    ;;TODO dynamic title
    :options   {:insets {:top? true :bottom? true}}
    :component add-account/add-account-view}
   {:name      :add-new-account-pin
    ;;TODO dynamic title
    :options   {:insets {:top? true}}
    :component add-account/pin}
   {:name      :account-settings
    ;;TODO dynamic title
    :options   {:insets {:top? true}}
    :component account-settings/account-settings}
   {:name      :wallet-transaction-details
    ;;TODO dynamic title
    :options   {:insets {:top? true}}
    :component wallet-transactions/transaction-details}
   {:name      :wallet-settings-assets
    ;;TODO dynamic title
    :options   {:insets {:top? true}}
    :component wallet-settings/manage-assets}
   {:name      :wallet-add-custom-token
    :on-focus  [:wallet-legacy/wallet-add-custom-token]
    :options   {:topBar {:title {:text (i18n/label :t/add-custom-token)}}
                :insets {:top? true}}
    :component custom-tokens/add-custom-token}
   {:name      :wallet-custom-token-details
    ;;TODO dynamic title
    :options   {:insets {:top? true}}
    :component custom-tokens/custom-token-details}
   {:name      :currency-settings
    :options   {:topBar {:title {:text (i18n/label :t/main-currency)}}
                :insets {:top? true}}
    :component currency-settings/currency-settings}

   {:name      :manage-accounts
    :options   {:topBar {:title {:text (i18n/label :t/wallet-manage-accounts)}}
                :insets {:top? true}}
    :component accounts-manage/manage}

   {:name      :token-swap
    ;;TODO dynamic title
    :options   {:insets {:top? true}}
    :component wallet.swap/swap}

   {:name      :token-swap-advanced-nonce
    :options   {:topBar {:title {:text (i18n/label :t/nonce)}}
                :insets {:top? true}}
    :component wallet.swap/nonce-modal}

   {:name      :token-swap-advanced-approve-token
    :options   {:topBar {:title {:text (i18n/label :t/approve-token)}}
                :insets {:top? true}}
    :component wallet.swap/approve-token-modal}

   {:name      :token-swap-advanced-transaction-fee
    :options   {:topBar {:title {:text (i18n/label :t/transaction-fee)}}
                :insets {:top? true}}
    :component wallet.swap/transaction-fee-modal}

   {:name      :token-swap-advanced-swap-details
    :options   {:topBar {:title {:text (i18n/label :t/swap-details)}}
                :insets {:top? true}}
    :component wallet.swap/swap-details-modal}

   {:name      :swap-asset-selector
    ;;TODO dynamic title
    :options   {:insets {:top? true}}
    :component wallet.swap/asset-selector}

   ;;PROFILE

   {:name      :my-profile
    :options   {:topBar {:visible false}}
    :component profile.user/my-profile}
   {:name      :contacts-list
    :options   {:topBar {:title {:text (i18n/label :t/contacts)}}
                :insets {:top? true}}
    :component contacts-list/contacts-list}
   {:name      :ens-main
    :options   {:topBar {:title {:text (i18n/label :t/ens-usernames)}}
                :insets {:top? true}}
    :component ens/main}
   {:name      :ens-search
    :options   {:topBar {:title {:text (i18n/label :t/ens-your-username)}}
                :insets {:top? true}}
    :component ens/search}
   {:name      :ens-checkout
    :options   {:topBar {:title {:text (i18n/label :t/ens-your-username)}}
                :insets {:top? true}}
    :component ens/checkout}
   {:name      :ens-confirmation
    :options   {:topBar {:title {:text (i18n/label :t/ens-your-username)}}
                :insets {:top? true}}
    :component ens/confirmation}
   {:name      :ens-terms
    :options   {:topBar {:title {:text (i18n/label :t/ens-terms-registration)}}
                :insets {:top? true}}
    :component ens/terms}
   {:name      :ens-name-details
    ;;TODO dynamic title
    :options   {:insets {:top? true}}
    :component ens/name-details}
   {:name      :blocked-users-list
    :options   {:topBar {:title {:text (i18n/label :t/blocked-users)}}
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
    :options   {:topBar {:title {:text (i18n/label :t/devices)}}
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
    :options   {:topBar {:title {:text (i18n/label :t/dapps-permissions)}}
                :insets {:top? true}}
    :component dapps-permissions/dapps-permissions}
   {:name      :privacy-and-security
    :options   {:topBar {:title {:text (i18n/label :t/privacy-and-security)}}
                :insets {:top? true}}
    :component privacy-and-security/privacy-and-security}
   {:name      :messages-from-contacts-only
    :options   {:topBar {:title {:text (i18n/label :t/accept-new-chats-from)}}
                :insets {:top? true}}
    :component messages-from-contacts-only/messages-from-contacts-only-view}
   {:name      :appearance
    :options   {:topBar {:title {:text (i18n/label :t/appearance)}}
                :insets {:top? true}}
    :component appearance/appearance-view}
   {:name      :privacy-and-security-profile-pic-show-to
    :options   {:topbar {:title {:text (i18n/label :t/show-profile-pictures-to)}}
                :insets {:top? true}}
    :component privacy-and-security/profile-pic-show-to}
   {:name      :privacy-and-security-profile-pic
    :options   {:topBar {:title {:text (i18n/label :t/show-profile-pictures)}}
                :insets {:top? true}}
    :component privacy-and-security/profile-pic}
   {:name      :notifications
    :options   {:topBar {:title {:text (i18n/label :t/notification-settings)}}
                :insets {:top? true}}
    :component notifications-settings/notifications-settings}
   {:name      :sync-settings
    :options   {:topBar {:title {:text (i18n/label :t/sync-settings)}}
                :insets {:top? true}}
    :component sync-settings/sync-settings}
   {:name      :advanced-settings
    :options   {:topBar {:title {:text (i18n/label :t/advanced)}}
                :insets {:top? true}}
    :component advanced-settings/advanced-settings}
   {:name      :help-center
    :options   {:topBar {:title {:text (i18n/label :t/need-help)}}
                :insets {:top? true}}
    :component help-center/help-center}
   {:name      :glossary
    :options   {:topBar {:title {:text (i18n/label :t/glossary)}}
                :insets {:top? true}}
    :component glossary/glossary}
   {:name      :about-app
    :options   {:topBar {:title {:text (i18n/label :t/about-app)}}
                :insets {:top? true}}
    :component about-app/about-app}
   {:name      :privacy-policy
    :options   {:topBar {:title {:text (i18n/label :t/privacy-policy)}}
                :insets {:top? true}}
    :component about-app/privacy-policy}
   {:name      :terms-of-service
    :options   {:topBar {:title {:text (i18n/label :t/terms-of-service)}}
                :insets {:top? true}}
    :component about-app/tos}
   {:name      :principles
    :options   {:topBar {:title {:text (i18n/label :t/principles)}}
                :insets {:top? true}}
    :component about-app/principles}
   {:name      :manage-dapps-permissions
    ;;TODO dynamic title
    :options   {:insets {:top? true}}
    :component dapps-permissions/manage}
   {:name      :network-settings
    ;;TODO accessories
    :options   {:insets {:top? true}}
    :component network/network-settings}
   {:name      :network-details
    :options   {:topBar {:title {:text (i18n/label :t/network-details)}}
                :insets {:top? true}}
    :component network-details/network-details}
   {:name      :network-info
    :options   {:topBar {:title {:text (i18n/label :t/network-info)}}
                :insets {:top? true}}
    :component network-info/network-info}
   {:name      :rpc-usage-info
    :options   {:topBar {:title {:text (i18n/label :t/rpc-usage-info)}}
                :insets {:top? true}}
    :component rpc-usage-info/usage-info}
   {:name      :peers-stats
    :options   {:topBar {:title {:text (i18n/label :t/peers-stats)}}
                :insets {:top? true}}
    :component peers-stats/peers-stats}
   {:name      :edit-network
    :options   {:topBar {:title {:text (i18n/label :t/add-network)}}
                :insets {:top? true}}
    :component edit-network/edit-network}
   {:name      :log-level-settings
    :options   {:topBar {:title {:text (i18n/label :t/log-level-settings)}}
                :insets {:top? true}}
    :component log-level-settings/log-level-settings}
   {:name      :fleet-settings
    :options   {:topBar {:title {:text (i18n/label :t/fleet-settings)}}
                :insets {:top? true}}
    :component fleet-settings/fleet-settings}
   {:name      :mobile-network-settings
    :options   {:topBar {:title {:text (i18n/label :t/mobile-network-settings)}}
                :insets {:top? true}}
    :component mobile-network-settings/mobile-network-settings}
   {:name      :backup-settings
    :options   {:topBar {:title {:text (i18n/label :t/backup-settings)}}
                :insets {:top? true}}
    :component backup-settings/backup-settings}
   {:name      :backup-seed
    ;;TODO dynamic navigation
    :options   {:insets {:top? true}}
    :component profile.seed/backup-seed}
   {:name      :reset-password
    :options   {:topBar {:title {:text (i18n/label :t/reset-password)}}
                :insets {:top? true}}
    :component reset-password/reset-password}
   {:name      :delete-profile
    :insets    {:bottom? true}
    :component delete-profile/delete-profile}
   {:name      :default-sync-period-settings
    :options   {:topBar {:title {:text (i18n/label :t/default-sync-period)}}
                :insets {:top? true}}
    :component default-sync-period-settings/default-sync-period-settings}

   ;;MODALS

   ;[Chat] Link preview settings

   {:name      :link-previews-settings
    :options   {:topBar {:title {:text (i18n/label :t/chat-link-previews)}}
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

   ;[Wallet] Recipient
   {:name      :recipient
    ;;TODO accessories
    :options   {:insets {:bottom? true
                         :top?    true}}
    :component recipient/recipient}

   ;[Wallet] New favourite
   {:name      :new-favourite
    :options   {:topBar {:title {:text (i18n/label :t/new-favourite)}}
                :insets {:bottom? true
                         :top?    true}}
    :component recipient/new-favourite}

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
    :options   {:topBar             {:title {:text (i18n/label :t/notification-settings)}}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}
                :insets             {:bottom? true
                                     :top?    true}}
    :component notifications-settings/notifications-settings}

   ;[Wallet] Prepare Transaction
   {:name        :prepare-send-transaction
    :on-dissmiss [:wallet-legacy/cancel-transaction-command]
    :options     {:topBar             {:title {:text (i18n/label :t/send-transaction)}}
                  :swipeToDismiss     false
                  :hardwareBackButton {:dismissModalOnPress false}
                  :insets             {:bottom? true
                                       :top?    true}}
    :component   wallet.send/prepare-send-transaction}

   ;[Wallet] Request Transaction
   {:name        :request-transaction
    :on-dissmiss [:wallet-legacy/cancel-transaction-command]
    :options     {:topBar             {:title {:text (i18n/label :t/request-transaction)}}
                  :swipeToDismiss     false
                  :hardwareBackButton {:dismissModalOnPress false}
                  :insets             {:bottom? true
                                       :top?    true}}
    :component   wallet.send/request-transaction}

   ;[Wallet] Buy crypto
   {:name      :buy-crypto
    :insets    {:bottom? true}
    :component wallet.buy-crypto/container}

   ;[Wallet] Buy crypto website
   {:name      :buy-crypto-website
    ;;TODO subtitle
    :options   {:insets {:bottom? true
                         :top?    true}}
    :component wallet.buy-crypto/website}

   {:name      :nft-details
    ;;TODO dynamic title
    :options   {:insets {:bottom? true
                         :top?    true}}
    :component wallet.collectibles/nft-details-modal}

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

   ;KEYCARD
   {:name      :keycard-onboarding-intro
    :options   {:insets             {:bottom? true
                                     :top?    true}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :component keycard.onboarding/intro}
   {:name      :keycard-onboarding-puk-code
    :options   {:insets             {:bottom? true
                                     :top?    true}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    ;;TODO dynamic
    :component keycard.onboarding/puk-code}
   {:name      :keycard-onboarding-pin
    :options   {:insets             {:bottom? true
                                     :top?    true}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    ;;TODO dynamic
    :component keycard.onboarding/pin}
   {:name      :keycard-recovery-pair
    :options   {:topBar             {:title {:text (i18n/label :t/step-i-of-n {:number 2 :step 1})}}
                :insets             {:bottom? true
                                     :top?    true}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :component keycard.recovery/pair}

   {:name      :keycard-recovery-pin
    ;;TODO dynamic
    :options   {:insets {:bottom? true
                         :top?    true}}
    :component keycard.recovery/pin}
   {:name      :keycard-wrong
    ;;TODO move to popover?
    :options   {:insets {:bottom? true
                         :top?    true}}
    :component keycard/wrong}
   {:name      :not-keycard
    :options   {:insets {:bottom? true
                         :top?    true}}
    ;;TODO move to popover?
    :component keycard/not-keycard}
   {:name      :keycard-onboarding-recovery-phrase
    :options   {:insets             {:bottom? true
                                     :top?    true}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :component keycard.onboarding/recovery-phrase}
   {:name      :keycard-onboarding-recovery-phrase-confirm-word1
    :options   {:insets             {:bottom? true
                                     :top?    true}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :component keycard.onboarding/recovery-phrase-confirm-word}
   {:name      :keycard-onboarding-recovery-phrase-confirm-word2
    :options   {:insets             {:bottom? true
                                     :top?    true}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :component keycard.onboarding/recovery-phrase-confirm-word}
   {:name      :keycard-recovery-intro
    :insets    {:bottom? true}
    :component keycard.recovery/intro}
   {:name      :keycard-recovery-success
    :options   {:insets             {:bottom? true
                                     :top?    true}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :component keycard.recovery/success}
   {:name      :keycard-recovery-no-key
    :options   {:insets             {:bottom? true
                                     :top?    true}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :component keycard.recovery/no-key}
   {:name      :keycard-authentication-method
    :options   {:insets {:bottom? true
                         :top?    true}}
    :component keycard.authentication/keycard-authentication-method}
   {:name      :keycard-login-pin
    :options   {:insets {:bottom? true
                         :top?    true}}
    :component keycard/login-pin}
   {:name      :keycard-blank
    :options   {:insets {:bottom? true
                         :top?    true}}
    :component keycard/blank}
   {:name      :keycard-unpaired
    :options   {:insets {:bottom? true
                         :top?    true}}
    :component keycard/unpaired}
   {:name      :keycard-settings
    :options   {:topBar {:title {:text (i18n/label :t/status-keycard)}}
                :insets {:bottom? true
                         :top?    true}}
    :component keycard.settings/keycard-settings}
   {:name      :reset-card
    :options   {:topBar {:title {:text (i18n/label :t/reset-card)}}
                :insets {:bottom? true
                         :top?    true}}
    :component keycard.settings/reset-card}
   {:name      :keycard-pin
    ;;TODO dynamic title
    :options   {:insets {:bottom? true
                         :top?    true}}
    :component keycard.settings/reset-pin}
   {:name      :enter-pin-settings
    :insets    {:bottom? true}
    :component keycard.pin/enter-pin}
   {:name      :change-pairing-code
    :insets    {:bottom? true}
    :component keycard.pairing/change-pairing-code}

   ;; BUG REPORT
   {:name      :bug-report
    :options   {:insets {:top? true}}
    :component bug-report/bug-report}])
