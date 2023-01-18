(ns status-im.ui.screens.screens
  (:require
   [quo.design-system.colors :as colors]
   [status-im.add-new.core :as new-chat.events]
   [i18n.i18n :as i18n]
   [status-im.ui.components.icons.icons :as icons]
   [status-im.ui.screens.about-app.views :as about-app]
   [status-im.ui.screens.add-new.new-chat.views :as new-chat]
   [status-im.ui.screens.add-new.new-public-chat.view :as new-public-chat]
   [status-im.ui.screens.advanced-settings.views :as advanced-settings]
   [status-im.ui.screens.appearance.views :as appearance]
   [status-im.ui.screens.backup-settings.view :as backup-settings]
   [status-im.ui.screens.bootnodes-settings.edit-bootnode.views :as edit-bootnode]
   [status-im.ui.screens.bootnodes-settings.views :as bootnodes-settings]
   [status-im.ui.screens.browser.bookmarks.views :as bookmarks]
   [status-im.ui.screens.browser.empty-tab.views :as empty-tab]
   [status-im.ui.screens.browser.tabs.views :as browser.tabs]
   [status-im.ui.screens.browser.views :as browser]
   [status-im.ui.screens.bug-report :as bug-report]
   [status-im.ui.screens.communities.channel-details :as communities.channel-details]
   [status-im.ui.screens.communities.community :as community]
   [status-im.ui.screens.communities.community-emoji-thumbnail-picker :as
    community-emoji-thumbnail-picker]
   [status-im.ui.screens.communities.create :as communities.create]
   [status-im.ui.screens.communities.create-category :as create-category]
   [status-im.ui.screens.communities.create-channel :as create-channel]
   [status-im.ui.screens.communities.edit :as community.edit]
   [status-im.ui.screens.communities.edit-channel :as edit-channel]
   [status-im.ui.screens.communities.import :as communities.import]
   [status-im.ui.screens.communities.invite :as communities.invite]
   [status-im.ui.screens.communities.members :as members]
   [status-im.ui.screens.communities.membership :as membership]
   [status-im.ui.screens.communities.profile :as community.profile]
   [status-im.ui.screens.communities.reorder-categories :as reorder-categories]
   [status-im.ui.screens.communities.requests-to-join :as requests-to-join]
   [status-im.ui.screens.communities.select-category :as select-category]
   [status-im.ui.screens.communities.views :as communities]
   [status-im.ui.screens.contacts-list.views :as contacts-list]
   [status-im.ui.screens.currency-settings.views :as currency-settings]
   [status-im.ui.screens.dapps-permissions.views :as dapps-permissions]
   [status-im.ui.screens.default-sync-period-settings.view :as default-sync-period-settings]
   [status-im.ui.screens.ens.views :as ens]
   [status-im.ui.screens.fleet-settings.views :as fleet-settings]
   [status-im.ui.screens.glossary.view :as glossary]
   [status-im.ui.screens.group.views :as group-chat]
   [status-im.ui.screens.help-center.views :as help-center]
   [status-im.ui.screens.keycard.authentication-method.views :as keycard.authentication]
   [status-im.ui.screens.keycard.onboarding.views :as keycard.onboarding]
   [status-im.ui.screens.keycard.pairing.views :as keycard.pairing]
   [status-im.ui.screens.keycard.pin.views :as keycard.pin]
   [status-im.ui.screens.keycard.recovery.views :as keycard.recovery]
   [status-im.ui.screens.keycard.settings.views :as keycard.settings]
   [status-im.ui.screens.keycard.views :as keycard]
   [status-im.ui.screens.link-previews-settings.views :as link-previews-settings]
   [status-im.ui.screens.log-level-settings.views :as log-level-settings]
   [status-im.ui.screens.mobile-network-settings.view :as mobile-network-settings]
   [status-im.ui.screens.multiaccounts.key-storage.views :as key-storage.views]
   [status-im.ui.screens.multiaccounts.login.views :as login]
   [status-im.ui.screens.multiaccounts.views :as multiaccounts]
   [status-im.ui.screens.network-info.views :as network-info]
   [status-im.ui.screens.network.edit-network.views :as edit-network]
   [status-im.ui.screens.network.network-details.views :as network-details]
   [status-im.ui.screens.network.views :as network]
   [status-im.ui.screens.notifications-settings.views :as notifications-settings]
   [status-im.ui.screens.offline-messaging-settings.edit-mailserver.views :as edit-mailserver]
   [status-im.ui.screens.offline-messaging-settings.views :as offline-messaging-settings]
   [status-im.ui.screens.onboarding.intro.views :as onboarding.intro]
   [status-im.ui.screens.onboarding.keys.views :as onboarding.keys]
   [status-im.ui.screens.onboarding.notifications.views :as onboarding.notifications]
   [status-im.ui.screens.onboarding.password.views :as onboarding.password]
   [status-im.ui.screens.onboarding.phrase.view :as onboarding.phrase]
   [status-im.ui.screens.onboarding.storage.views :as onboarding.storage]
   [status-im.ui.screens.onboarding.welcome.views :as onboarding.welcome]
   [status-im.ui.screens.pairing.views :as pairing]
   [status-im.ui.screens.peers-stats :as peers-stats]
   [status-im.ui.screens.privacy-and-security-settings.delete-profile :as delete-profile]
   [status-im.ui.screens.privacy-and-security-settings.messages-from-contacts-only :as
    messages-from-contacts-only]
   [status-im.ui.screens.privacy-and-security-settings.views :as privacy-and-security]
   [status-im.ui.screens.profile.contact.views :as contact]
   [status-im.ui.screens.profile.group-chat.views :as profile.group-chat]
   [status-im.ui.screens.profile.seed.views :as profile.seed]
   [status-im.ui.screens.profile.user.views :as profile.user]
   [status-im.ui.screens.progress.views :as progress]
   [status-im.ui.screens.qr-scanner.views :as qr-scanner]
   [status-im.ui.screens.reset-password.views :as reset-password]
   [status-im.ui.screens.rpc-usage-info :as rpc-usage-info]
   [status-im.ui.screens.stickers.views :as stickers]
   [status-im.ui.screens.sync-settings.views :as sync-settings]
   [status-im.ui.screens.terms-of-service.views :as terms-of-service]
   [status-im.ui.screens.wakuv2-settings.edit-node.views :as edit-wakuv2-node]
   [status-im.ui.screens.wakuv2-settings.views :as wakuv2-settings]
   [status-im.ui.screens.wallet.account-settings.views :as account-settings]
   [status-im.ui.screens.wallet.account.views :as wallet.account]
   [status-im.ui.screens.wallet.accounts-manage.views :as accounts-manage]
   [status-im.ui.screens.wallet.accounts.views :as wallet.accounts]
   [status-im.ui.screens.wallet.add-new.views :as add-account]
   [status-im.ui.screens.wallet.buy-crypto.views :as wallet.buy-crypto]
   [status-im.ui.screens.wallet.collectibles.views :as wallet.collectibles]
   [status-im.ui.screens.wallet.custom-tokens.views :as custom-tokens]
   [status-im.ui.screens.wallet.manage-connections.views :as manage-all-connections]
   [status-im.ui.screens.wallet.recipient.views :as recipient]
   [status-im.ui.screens.wallet.send.views :as wallet.send]
   [status-im.ui.screens.wallet.settings.views :as wallet-settings]
   [status-im.ui.screens.wallet.swap.views :as wallet.swap]
   [status-im.ui.screens.wallet.transactions.views :as wallet-transactions]
   [status-im2.contexts.chat.group-details.view :as group-details]
   [status-im.ui2.screens.chat.components.new-chat.view :as new-chat-aio]))

(defn right-button-options
  [id icon]
  {:id   id
   :icon (icons/icon-source icon)})

(defn screens
  []
  [;;INTRO, ONBOARDING, LOGIN

   ;Multiaccounts
   {:name          :multiaccounts
    :insets        {:bottom true}
    :options       {:topBar {:title        {:text (i18n/label :t/your-keys)}
                             :rightButtons (right-button-options :multiaccounts :more)}}
    :right-handler multiaccounts/topbar-button
    :component     multiaccounts/multiaccounts}

   ;Login
   {:name          :login
    :insets        {:bottom true}
    :options       {:topBar {:rightButtons (right-button-options :login :more)}}
    :right-handler login/topbar-button
    :component     login/login}

   {:name      :progress
    :component progress/progress}

   ;[Onboarding]
   {:name      :intro
    :insets    {:bottom true}
    :component onboarding.intro/intro}

   ;[Onboarding]
   {:name      :get-your-keys
    :insets    {:bottom true}
    :component onboarding.keys/get-your-keys}

   ;[Onboarding]
   {:name      :choose-name
    :options   {:topBar             {:visible false}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :insets    {:bottom true}
    :component onboarding.keys/choose-a-chat-name}

   ;[Onboarding]
   {:name      :select-key-storage
    :insets    {:bottom true}
    :options   {:popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :component onboarding.storage/select-key-storage}

   ;[Onboarding] Create Password
   {:name      :create-password
    :options   {:popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :insets    {:bottom true}
    :component onboarding.password/screen}

   ;[Onboarding] Welcome
   {:name      :welcome
    :options   {:popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :insets    {:bottom true}
    :component onboarding.welcome/welcome}

   ;[Onboarding] Notification
   {:name      :onboarding-notification
    :options   {:popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :insets    {:bottom true}
    :component onboarding.notifications/notifications-onboarding}

   ;[Onboarding] Recovery
   {:name      :recover-multiaccount-enter-phrase
    :insets    {:bottom true}
    :component onboarding.phrase/enter-phrase}
   {:name      :recover-multiaccount-success
    :options   {:popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :insets    {:bottom true}
    :component onboarding.phrase/wizard-recovery-success}

   ;;CHAT

   {:name      :group-chat-profile
    ;;TODO animated-header
    :options   {:topBar {:visible false}}
    :component group-details/group-details}
   {:name      :group-chat-invite
    ;;TODO parameter in the event
    :options   {:topBar {:visible false}}
    :component profile.group-chat/group-chat-invite}

   {:name      :stickers
    :options   {:topBar {:title {:text (i18n/label :t/sticker-market)}}}
    :component stickers/packs}

   {:name      :stickers-pack
    :component stickers/pack}

   ;; Community
   {:name      :community
    ;;TODO custom
    :options   {:topBar {:visible false}}
    :component community/community}
   {:name      :community-management
    :insets    {:top false}
    ;;TODO animated-header
    :options   {:topBar {:visible false}}
    :component community.profile/management-container}
   {:name      :community-members
    ;;TODO custom subtitle
    :options   {:topBar {:visible false}}
    :component members/members-container}
   {:name      :community-requests-to-join
    ;;TODO custom subtitle
    :options   {:topBar {:visible false}}
    :component requests-to-join/requests-to-join-container}
   {:name      :create-community-channel
    :insets    {:bottom true}
    :options   {:topBar {:title {:text (i18n/label :t/create-channel-title)}}}
    :component create-channel/view}
   {:name      :community-emoji-thumbnail-picker
    :insets    {:bottom true}
    :options   {:topBar {:title {:text (i18n/label :t/community-emoji-thumbnail-title)}}}
    :component community-emoji-thumbnail-picker/view}
   {:name      :create-community-category
    :insets    {:bottom true}
    :options   {:topBar {:title {:text (i18n/label :t/new-category)}}}
    :component create-category/view}
   {:name      :select-category
    :insets    {:bottom true}
    ;;TODO custom
    :options   {:topBar {:visible false}}
    :component select-category/view}
   {:name      :community-reorder-categories
    :insets    {:top false}
    :options   {:topBar {:visible false}}
    :component reorder-categories/view}
   {:name      :community-channel-details
    :insets    {:top false}
    ;;TODO custom
    :options   {:topBar {:visible false}}
    :component communities.channel-details/view}
   {:name      :edit-community-channel
    :options   {:topBar {:title {:text (i18n/label :t/edit-channel-title)}}}
    :insets    {:bottom true}
    :component edit-channel/view}
   {:name      :contact-toggle-list
    ;;TODO custom subtitle
    :options   {:topBar {:visible false}}
    :component group-chat/contact-toggle-list}
   {:name      :new-group
    :options   {:topBar {:visible false}}
    ;;TODO custom subtitle
    :component group-chat/new-group}
   {:name      :communities
    ;;TODO custom
    :options   {:topBar {:visible false}}
    :component communities/communities}
   {:name      :community-import
    :options   {:topBar {:title {:text (i18n/label :t/import-community-title)}}}
    :component communities.import/view}
   {:name      :community-edit
    :options   {:topBar {:title {:text (i18n/label :t/community-edit-title)}}}
    :component community.edit/edit}
   {:name      :community-create
    :options   {:topBar {:title {:text (i18n/label :t/new-community-title)}}}
    :component communities.create/view}
   {:name      :community-membership
    :options   {:topBar {:title {:text (i18n/label :t/membership-title)}}}
    :component membership/membership}

   ;;BROWSER

   {:name      :empty-tab
    :insets    {:top true}
    :options   {:topBar             {:visible false}
                :hardwareBackButton {:popStackOnPress false}}
    :component empty-tab/empty-tab}
   {:name      :browser
    :options   {:topBar             {:visible false}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :component browser/browser}
   {:name      :browser-tabs
    :insets    {:top true}
    :options   {:topBar             {:visible false}
                :hardwareBackButton {:popStackOnPress false}}
    :component browser.tabs/tabs}

   ;;WALLET

   {:name      :wallet
    :insets    {:top false}
    :on-focus  [:wallet/tab-opened]
    ;;TODO wallet redesign
    ;;:options   {:statusBar {:backgroundColor quo2.colors/neutral-5}}
    :component wallet.accounts/accounts-overview-old}
   {:name      :wallet-account
    ;;TODO dynamic titleaccounts-overview
    :options   {:topBar {:visible false}}
    :component wallet.account/account}
   {:name      :add-new-account
    ;;TODO dynamic title
    :options   {:topBar {:visible false}}
    :component add-account/add-account}
   {:name      :add-new-account-pin
    ;;TODO dynamic title
    :options   {:topBar {:visible false}}
    :component add-account/pin}
   {:name      :account-settings
    ;;TODO dynamic title
    :options   {:topBar {:visible false}}
    :component account-settings/account-settings}
   {:name      :wallet-transaction-details
    ;;TODO dynamic title
    :options   {:topBar {:visible false}}
    :component wallet-transactions/transaction-details}
   {:name      :wallet-settings-assets
    ;;TODO dynamic title
    :options   {:topBar {:visible false}}
    :component wallet-settings/manage-assets}
   {:name      :wallet-add-custom-token
    :on-focus  [:wallet/wallet-add-custom-token]
    :options   {:topBar {:title {:text (i18n/label :t/add-custom-token)}}}
    :component custom-tokens/add-custom-token}
   {:name      :wallet-custom-token-details
    ;;TODO dynamic title
    :options   {:topBar {:visible false}}
    :component custom-tokens/custom-token-details}
   {:name      :currency-settings
    :options   {:topBar {:title {:text (i18n/label :t/main-currency)}}}
    :component currency-settings/currency-settings}

   {:name      :manage-accounts
    :options   {:topBar {:title {:text (i18n/label :t/wallet-manage-accounts)}}}
    :component accounts-manage/manage}

   {:name      :token-swap
    ;;TODO dynamic title
    :options   {:topBar {:visible false}}
    :component wallet.swap/swap}

   {:name      :token-swap-advanced-nonce
    :options   {:topBar {:title {:text (i18n/label :t/nonce)}}}
    :component wallet.swap/nonce-modal}

   {:name      :token-swap-advanced-approve-token
    :options   {:topBar {:title {:text (i18n/label :t/approve-token)}}}
    :component wallet.swap/approve-token-modal}

   {:name      :token-swap-advanced-transaction-fee
    :options   {:topBar {:title {:text (i18n/label :t/transaction-fee)}}}
    :component wallet.swap/transaction-fee-modal}

   {:name      :token-swap-advanced-swap-details
    :options   {:topBar {:title {:text (i18n/label :t/swap-details)}}}
    :component wallet.swap/swap-details-modal}

   {:name      :swap-asset-selector
    ;;TODO dynamic title
    :options   {:topBar {:visible false}}
    :component wallet.swap/asset-selector}

   ;;PROFILE

   {:name      :my-profile
    :insets    {:top false}
    :options   {:topBar {:visible false}}
    :component profile.user/my-profile}
   {:name      :contacts-list
    :options   {:topBar {:title {:text (i18n/label :t/contacts)}}}
    :component contacts-list/contacts-list}
   {:name      :ens-main
    :options   {:topBar {:title {:text (i18n/label :t/ens-usernames)}}}
    :component ens/main}
   {:name      :ens-search
    :options   {:topBar {:title {:text (i18n/label :t/ens-your-username)}}}
    :component ens/search}
   {:name      :ens-checkout
    :options   {:topBar {:title {:text (i18n/label :t/ens-your-username)}}}
    :component ens/checkout}
   {:name      :ens-confirmation
    :options   {:topBar {:title {:text (i18n/label :t/ens-your-username)}}}
    :component ens/confirmation}
   {:name      :ens-terms
    :options   {:topBar {:title {:text (i18n/label :t/ens-terms-registration)}}}
    :component ens/terms}
   {:name      :ens-name-details
    ;;TODO dynamic title
    :options   {:topBar {:visible false}}
    :component ens/name-details}
   {:name      :blocked-users-list
    :options   {:topBar {:title {:text (i18n/label :t/blocked-users)}}}
    :component contacts-list/blocked-users-list}
   {:name      :wakuv2-settings
    ;;TODO dynamic title
    :options   {:topBar {:visible false}}
    :component wakuv2-settings/wakuv2-settings}
   {:name      :edit-wakuv2-node
    ;;TODO dynamic title
    :options   {:topBar {:visible false}}
    :component edit-wakuv2-node/edit-node}
   {:name      :bootnodes-settings
    ;;TODO dynamic title
    :options   {:topBar {:visible false}}
    :component bootnodes-settings/bootnodes-settings}
   {:name      :installations
    :options   {:topBar {:title {:text (i18n/label :t/devices)}}}
    :component pairing/installations}
   {:name      :edit-bootnode
    ;;TODO dynamic title
    :options   {:topBar {:visible false}}
    :component edit-bootnode/edit-bootnode}
   {:name      :offline-messaging-settings
    ;;TODO dynamic title
    :options   {:topBar {:visible false}}
    :component offline-messaging-settings/offline-messaging-settings}
   {:name      :edit-mailserver
    ;;TODO dynamic title
    :options   {:topBar {:visible false}}
    :component edit-mailserver/edit-mailserver}
   {:name      :dapps-permissions
    :options   {:topBar {:title {:text (i18n/label :t/dapps-permissions)}}}
    :component dapps-permissions/dapps-permissions}
   {:name      :link-previews-settings
    :options   {:topBar {:title {:text (i18n/label :t/chat-link-previews)}}}
    :component link-previews-settings/link-previews-settings}
   {:name      :privacy-and-security
    :options   {:topBar {:title {:text (i18n/label :t/privacy-and-security)}}}
    :component privacy-and-security/privacy-and-security}
   {:name      :messages-from-contacts-only
    :options   {:topBar {:title {:text (i18n/label :t/accept-new-chats-from)}}}
    :component messages-from-contacts-only/messages-from-contacts-only}
   {:name      :appearance
    :options   {:topBar {:title {:text (i18n/label :t/appearance)}}}
    :component appearance/appearance}
   {:name      :privacy-and-security-profile-pic-show-to
    :options   {:topbar {:title {:text (i18n/label :t/show-profile-pictures-to)}}}
    :component privacy-and-security/profile-pic-show-to}
   {:name      :privacy-and-security-profile-pic
    :options   {:topBar {:title {:text (i18n/label :t/show-profile-pictures)}}}
    :component privacy-and-security/profile-pic}
   {:name      :notifications
    :options   {:topBar {:title {:text (i18n/label :t/notification-settings)}}}
    :component notifications-settings/notifications-settings}
   {:name      :notifications-servers
    :options   {:topBar {:title {:text (i18n/label :t/notifications-servers)}}}
    :component notifications-settings/notifications-servers}
   {:name      :sync-settings
    :options   {:topBar {:title {:text (i18n/label :t/sync-settings)}}}
    :component sync-settings/sync-settings}
   {:name      :advanced-settings
    :options   {:topBar {:title {:text (i18n/label :t/advanced)}}}
    :component advanced-settings/advanced-settings}
   {:name      :help-center
    :options   {:topBar {:title {:text (i18n/label :t/need-help)}}}
    :component help-center/help-center}
   {:name      :glossary
    :options   {:topBar {:title {:text (i18n/label :t/glossary)}}}
    :component glossary/glossary}
   {:name      :about-app
    :options   {:topBar {:title {:text (i18n/label :t/about-app)}}}
    :component about-app/about-app}
   {:name      :privacy-policy
    :options   {:topBar {:title {:text (i18n/label :t/privacy-policy)}}}
    :component about-app/privacy-policy}
   {:name      :terms-of-service
    :options   {:topBar {:title {:text (i18n/label :t/terms-of-service)}}}
    :component about-app/tos}
   {:name      :principles
    :options   {:topBar {:title {:text (i18n/label :t/principles)}}}
    :component about-app/principles}
   {:name      :force-accept-tos
    :options   {:topBar {:visible false}}
    :component terms-of-service/force-accept-tos}
   {:name      :manage-dapps-permissions
    ;;TODO dynamic title
    :options   {:topBar {:visible false}}
    :component dapps-permissions/manage}
   {:name      :network-settings
    ;;TODO accessories
    :options   {:topBar {:visible false}}
    :component network/network-settings}
   {:name      :network-details
    :options   {:topBar {:title {:text (i18n/label :t/network-details)}}}
    :component network-details/network-details}
   {:name      :network-info
    :options   {:topBar {:title {:text (i18n/label :t/network-info)}}}
    :component network-info/network-info}
   {:name      :rpc-usage-info
    :options   {:topBar {:title {:text (i18n/label :t/rpc-usage-info)}}}
    :component rpc-usage-info/usage-info}
   {:name      :peers-stats
    :options   {:topBar {:title {:text (i18n/label :t/peers-stats)}}}
    :component peers-stats/peers-stats}
   {:name      :edit-network
    :options   {:topBar {:title {:text (i18n/label :t/add-network)}}}
    :component edit-network/edit-network}
   {:name      :log-level-settings
    :options   {:topBar {:title {:text (i18n/label :t/log-level-settings)}}}
    :component log-level-settings/log-level-settings}
   {:name      :fleet-settings
    :options   {:topBar {:title {:text (i18n/label :t/fleet-settings)}}}
    :component fleet-settings/fleet-settings}
   {:name      :mobile-network-settings
    :options   {:topBar {:title {:text (i18n/label :t/mobile-network-settings)}}}
    :component mobile-network-settings/mobile-network-settings}
   {:name      :backup-settings
    :options   {:topBar {:title {:text (i18n/label :t/backup-settings)}}}
    :component backup-settings/backup-settings}
   {:name      :backup-seed
    ;;TODO dynamic navigation
    :options   {:topBar {:visible false}}
    :component profile.seed/backup-seed}
   {:name      :reset-password
    :options   {:topBar {:title {:text (i18n/label :t/reset-password)}}}
    :component reset-password/reset-password}
   {:name      :delete-profile
    :insets    {:bottom true}
    :component delete-profile/delete-profile}
   {:name      :default-sync-period-settings
    :options   {:topBar {:title {:text (i18n/label :t/default-sync-period)}}}
    :component default-sync-period-settings/default-sync-period-settings}

   ;;MODALS

   ;[Chat] New Chat
   {:name      :new-chat
    :on-focus  [::new-chat.events/new-chat-focus]
    ;;TODO accessories
    :options   {:topBar {:visible false}}
    :component new-chat/new-chat}

   ;[Chat] New Public chat
   {:name      :new-public-chat
    :insets    {:bottom true}
    :options   {:topBar {:title {:text (i18n/label :t/new-public-group-chat)}}}
    :component new-public-chat/new-public-chat}

   ;[Chat] Link preview settings
   {:name      :link-preview-settings
    :options   {:topBar {:title {:text (i18n/label :t/chat-link-previews)}}}
    :component link-previews-settings/link-previews-settings}

   ;[Chat] Edit nickname
   {:name      :nickname
    :insets    {:bottom true}
    ;;TODO dyn subtitle
    :options   {:topBar {:visible false}}
    :component contact/nickname}

   {:name      :new-chat-aio
    :on-focus  [::new-chat.events/new-chat-focus]
    ;;TODO accessories
    :options   {:topBar {:visible false}}
    :component new-chat-aio/contact-selection-list}

   ;[Chat] New Public chat
   {:name      :new-public-chat
    :insets    {:bottom true}
    :options   {:topBar {:title {:text (i18n/label :t/new-public-group-chat)}}}
    :component new-public-chat/new-public-chat}

   ;[Group chat] Add participants
   {:name      :add-participants-toggle-list
    :on-focus  [:group/add-participants-toggle-list]
    :insets    {:bottom true}
    ;;TODO dyn subtitle
    :options   {:topBar {:visible false}}
    :component group-chat/add-participants-toggle-list}

   ;[Communities] Invite people
   {:name      :invite-people-community
    ;;TODO dyn title
    :options   {:topBar {:visible false}}
    :component communities.invite/invite
    :insets    {:bottom true}}

   ;New Contact
   {:name      :new-contact
    :on-focus  [::new-chat.events/new-chat-focus]
    ;;TODO accessories
    :options   {:topBar {:visible false}}
    :component new-chat/new-contact}

   ;[Wallet] Recipient
   {:name      :recipient
    :insets    {:bottom true}
    ;;TODO accessories
    :options   {:topBar {:visible false}}
    :component recipient/recipient}

   ;[Wallet] New favourite
   {:name      :new-favourite
    :insets    {:bottom true}
    :options   {:topBar {:title {:text (i18n/label :t/new-favourite)}}}
    :component recipient/new-favourite}

   ;QR Scanner
   {:name      :qr-scanner
    :insets    {:top false :bottom false}
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
                                     :popStackOnPress     false}}
    :insets    {:bottom true}
    :component notifications-settings/notifications-settings}

   ;;TODO WHY MODAL?
   ;[Profile] Notifications Advanced settings
   {:name      :notifications-advanced-settings
    :options   {:topBar             {:title {:text (i18n/label :t/notification-settings)}}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :insets    {:bottom true}
    :component notifications-settings/notifications-advanced-settings}

   ;[Wallet] Prepare Transaction
   {:name        :prepare-send-transaction
    :insets      {:bottom true}
    :on-dissmiss [:wallet/cancel-transaction-command]
    :options     {:topBar             {:title {:text (i18n/label :t/send-transaction)}}
                  :swipeToDismiss     false
                  :hardwareBackButton {:dismissModalOnPress false}}
    :component   wallet.send/prepare-send-transaction}

   ;[Wallet] Request Transaction
   {:name        :request-transaction
    :insets      {:bottom true}
    :on-dissmiss [:wallet/cancel-transaction-command]
    :options     {:topBar             {:title {:text (i18n/label :t/request-transaction)}}
                  :swipeToDismiss     false
                  :hardwareBackButton {:dismissModalOnPress false}}
    :component   wallet.send/request-transaction}

   ;[Wallet] Buy crypto
   {:name      :buy-crypto
    :insets    {:bottom true}
    :component wallet.buy-crypto/container}

   ;[Wallet] Buy crypto website
   {:name      :buy-crypto-website
    :insets    {:bottom true}
    ;;TODO subtitle
    :options   {:topBar {:visible false}}
    :component wallet.buy-crypto/website}

   {:name      :nft-details
    :insets    {:bottom true}
    ;;TODO dynamic title
    :options   {:topBar {:visible false}}
    :component wallet.collectibles/nft-details-modal}

   ;[Browser] New bookmark
   {:name      :new-bookmark
    :insets    {:bottom true}
    ;;TODO dynamic title
    :options   {:topBar {:visible false}}
    :component bookmarks/new-bookmark}

   ;Profile
   {:name      :profile
    :insets    {:bottom true}
    ;;TODO custom toolbar
    :options   {:topBar {:visible false}}
    :component contact/profile}

   ;KEYCARD
   {:name      :keycard-onboarding-intro
    :insets    {:bottom true}
    :options   {:topBar             {:visible false}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :component keycard.onboarding/intro}
   {:name      :keycard-onboarding-puk-code
    :insets    {:bottom true}
    :options   {:topBar             {:visible false}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    ;;TODO dynamic
    :component keycard.onboarding/puk-code}
   {:name      :keycard-onboarding-pin
    :insets    {:bottom true}
    :options   {:topBar             {:visible false}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    ;;TODO dynamic
    :component keycard.onboarding/pin}
   {:name      :keycard-recovery-pair
    :insets    {:bottom true}
    :options   {:topBar             {:title {:text (i18n/label :t/step-i-of-n {:number 2 :step 1})}}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :component keycard.recovery/pair}
   {:name      :seed-phrase
    :insets    {:bottom true}
    ;;TODO subtitle
    :options   {:topBar {:visible false}}
    :component key-storage.views/seed-phrase}
   {:name      :keycard-recovery-pin
    :insets    {:bottom true}
    ;;TODO dynamic
    :options   {:topBar {:visible false}}
    :component keycard.recovery/pin}
   {:name      :keycard-wrong
    :insets    {:bottom true}
    ;;TODO move to popover?
    :options   {:topBar {:visible false}}
    :component keycard/wrong}
   {:name      :not-keycard
    :insets    {:bottom true}
    :options   {:topBar {:visible false}}
    ;;TODO move to popover?
    :component keycard/not-keycard}
   {:name      :keycard-onboarding-recovery-phrase
    :insets    {:bottom true}
    :options   {:topBar             {:visible false}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :component keycard.onboarding/recovery-phrase}
   {:name      :keycard-onboarding-recovery-phrase-confirm-word1
    :insets    {:bottom true}
    :options   {:topBar             {:visible false}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :component keycard.onboarding/recovery-phrase-confirm-word}
   {:name      :keycard-onboarding-recovery-phrase-confirm-word2
    :insets    {:bottom true}
    :options   {:topBar             {:visible false}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :component keycard.onboarding/recovery-phrase-confirm-word}
   {:name      :keycard-recovery-intro
    :insets    {:bottom true}
    :component keycard.recovery/intro}
   {:name      :keycard-recovery-success
    :insets    {:bottom true}
    :options   {:topBar             {:visible false}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :component keycard.recovery/success}
   {:name      :keycard-recovery-no-key
    :insets    {:bottom true}
    :options   {:topBar             {:visible false}
                :popGesture         false
                :hardwareBackButton {:dismissModalOnPress false
                                     :popStackOnPress     false}}
    :component keycard.recovery/no-key}
   {:name      :keycard-authentication-method
    :insets    {:bottom true}
    :options   {:topBar {:visible false}}
    :component keycard.authentication/keycard-authentication-method}
   {:name      :keycard-login-pin
    :insets    {:bottom true}
    :options   {:topBar {:visible false}}
    :component keycard/login-pin}
   {:name      :keycard-blank
    :insets    {:bottom true}
    :options   {:topBar {:visible false}}
    :component keycard/blank}
   {:name      :keycard-unpaired
    :insets    {:bottom true}
    :options   {:topBar {:visible false}}
    :component keycard/unpaired}
   {:name      :keycard-settings
    :insets    {:bottom true}
    :options   {:topBar {:title {:text (i18n/label :t/status-keycard)}}}
    :component keycard.settings/keycard-settings}
   {:name      :reset-card
    :insets    {:bottom true}
    :options   {:topBar {:title {:text (i18n/label :t/reset-card)}}}
    :component keycard.settings/reset-card}
   {:name      :keycard-pin
    :insets    {:bottom true}
    ;;TODO dynamic title
    :options   {:topBar {:visible false}}
    :component keycard.settings/reset-pin}
   {:name      :enter-pin-settings
    :insets    {:bottom true}
    :component keycard.pin/enter-pin}
   {:name      :change-pairing-code
    :insets    {:bottom true}
    :component keycard.pairing/change-pairing-code}

   ;;KEYSTORAGE
   {:name      :actions-not-logged-in
    ;;TODO: topbar
    :insets    {:bottom true}
    :options   {:topBar {:visible false}}
    ;;TODO move to popover?
    :component key-storage.views/actions-not-logged-in}
   {:name      :actions-logged-in
    ;;TODO: topbar
    :options   {:topBar {:visible false}}
    :insets    {:bottom true}
    ;;TODO move to popover?
    :component key-storage.views/actions-logged-in}
   {:name      :storage
    ;;TODO: topbar
    :options   {:topBar {:visible false}}
    :insets    {:bottom true}
    ;;TODO move to popover?
    :component key-storage.views/storage}

   {:name      :show-all-connections
    :insets    {:bottom true}
    :options   {:topBar {:title {:text (i18n/label :all-connections)}}}
    :component manage-all-connections/views}

   ;; BUG REPORT
   {:name      :bug-report
    :options   {:topBar {:visible false}}
    :component bug-report/bug-report}])
