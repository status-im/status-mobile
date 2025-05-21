(ns status-im.subs.root
  (:require
    status-im.domain.subs
    status-im.subs.activity-center
    status-im.subs.alert-banner
    status-im.subs.biometrics
    status-im.subs.chats
    status-im.subs.communities
    status-im.subs.community.account-selection
    status-im.subs.contact
    status-im.subs.general
    status-im.subs.keycard
    status-im.subs.messages
    status-im.subs.onboarding
    status-im.subs.pairing
    status-im.subs.profile
    status-im.subs.settings
    status-im.subs.shell
    status-im.subs.standard-authentication
    status-im.subs.wallet.activities
    status-im.subs.wallet.buy
    status-im.subs.wallet.collectibles
    status-im.subs.wallet.dapps.core
    status-im.subs.wallet.networks
    status-im.subs.wallet.saved-addresses
    status-im.subs.wallet.send
    status-im.subs.wallet.swap
    status-im.subs.wallet.wallet
    status-im.ui.market.subs
    [utils.re-frame :as rf]))

;;view
(rf/reg-root-key-sub :view-id :view-id)
(rf/reg-root-key-sub :screen-params :navigation/screen-params)
(rf/reg-root-key-sub :animation-shared-element-id :animation-shared-element-id)

;;bottom sheet
(rf/reg-root-key-sub :bottom-sheet :bottom-sheet)

;;media-server
(rf/reg-root-key-sub :mediaserver/port :mediaserver/port)

;;push notifications
(rf/reg-root-key-sub :push-notifications/preferences :push-notifications/preferences)

;;device
(rf/reg-root-key-sub :network/status :network/status)
(rf/reg-root-key-sub :network/type :network/type)

;;general
(rf/reg-root-key-sub :messenger/started? :messenger/started?)
(rf/reg-root-key-sub :animations :animations)
(rf/reg-root-key-sub :toasts :toasts)
(rf/reg-root-key-sub :popover/popover :popover/popover)
(rf/reg-root-key-sub :auth-method :auth-method)
(rf/reg-root-key-sub :syncing :syncing)
(rf/reg-root-key-sub :sync-state :sync-state)
(rf/reg-root-key-sub :dimensions/window :dimensions/window)
(rf/reg-root-key-sub :sync-data :sync-data)
(rf/reg-root-key-sub :mobile-network/remember-choice? :mobile-network/remember-choice?)
(rf/reg-root-key-sub :qr-modal :qr-modal)
(rf/reg-root-key-sub :bootnodes/manage :bootnodes/manage)
(rf/reg-root-key-sub :wakuv2-nodes/manage :wakuv2-nodes/manage)
(rf/reg-root-key-sub :wakuv2-nodes/list :wakuv2-nodes/list)
(rf/reg-root-key-sub :networks/current-network :networks/current-network)
(rf/reg-root-key-sub :networks/networks :networks/networks)
(rf/reg-root-key-sub :networks/manage :networks/manage)
(rf/reg-root-key-sub :get-pairing-installations :pairing/installations)
(rf/reg-root-key-sub :tooltips :tooltips)
(rf/reg-root-key-sub :app-state :app-state)
(rf/reg-root-key-sub :home-items-show-number :home-items-show-number)
(rf/reg-root-key-sub :password-authentication :password-authentication)
(rf/reg-root-key-sub :initials-avatar-font-file :initials-avatar-font-file)
(rf/reg-root-key-sub :alert-banners :alert-banners)
(rf/reg-root-key-sub :alert-banners/hide? :alert-banners/hide?)
(rf/reg-root-key-sub :currencies :currencies)
(rf/reg-root-key-sub :enter-seed-phrase/error :enter-seed-phrase/error)
(rf/reg-root-key-sub :privacy-mode/privacy-mode-enabled? :privacy-mode/privacy-mode-enabled?)

;;onboarding
(rf/reg-root-key-sub :onboarding/generated-keys? :onboarding/generated-keys?)
(rf/reg-root-key-sub :onboarding/new-account? :onboarding/new-account?)
(rf/reg-root-key-sub :onboarding/profile :onboarding/profile)

;;my profile
(rf/reg-root-key-sub :my-profile/seed :my-profile/seed)
;;profiles
(rf/reg-root-key-sub :profile/profiles-overview :profile/profiles-overview)
(rf/reg-root-key-sub :profile/login :profile/login)
(rf/reg-root-key-sub :profile/profile :profile/profile)
(rf/reg-root-key-sub :profile/logging-out? :profile/logging-out?)
(rf/reg-root-key-sub :profile/wallet-accounts :profile/wallet-accounts)

(rf/reg-root-key-sub :multiaccount/reset-password-form-vals :multiaccount/reset-password-form-vals)
(rf/reg-root-key-sub :multiaccount/reset-password-errors :multiaccount/reset-password-errors)
(rf/reg-root-key-sub :multiaccount/resetting-password? :multiaccount/resetting-password?)

;;chat
(rf/reg-root-key-sub :chats/cooldown-enabled? :chat/cooldown-enabled?)
(rf/reg-root-key-sub :chats/chats :chats)
(rf/reg-root-key-sub :chats/current-chat-id :current-chat-id)
(rf/reg-root-key-sub :public-group-topic :public-group-topic)
(rf/reg-root-key-sub :new-chat-name :new-chat-name)
(rf/reg-root-key-sub :chat/inputs :chat/inputs)
(rf/reg-root-key-sub :chat/memberships :chat/memberships)
(rf/reg-root-key-sub :group-chat/invitations :group-chat/invitations)
(rf/reg-root-key-sub :chats/mention-suggestions :chats/mention-suggestions)

(rf/reg-root-key-sub :chats-home-list :chats-home-list)
(rf/reg-root-key-sub :chats/recording? :chats/recording?)
(rf/reg-root-key-sub :reactions/authors :reactions/authors)

;;chat images lightbox
(rf/reg-root-key-sub :lightbox/exit-signal :lightbox/exit-signal)
(rf/reg-root-key-sub :lightbox/zoom-out-signal :lightbox/zoom-out-signal)
(rf/reg-root-key-sub :lightbox/orientation :lightbox/orientation)
(rf/reg-root-key-sub :lightbox/scale :lightbox/scale)

;;chat images camera roll
(rf/reg-root-key-sub :camera-roll/photos :camera-roll/photos)
(rf/reg-root-key-sub :camera-roll/end-cursor :camera-roll/end-cursor)
(rf/reg-root-key-sub :camera-roll/has-next-page :camera-roll/has-next-page)
(rf/reg-root-key-sub :camera-roll/loading-more :camera-roll/loading-more)
(rf/reg-root-key-sub :camera-roll/albums :camera-roll/albums)
(rf/reg-root-key-sub :camera-roll/selected-album :camera-roll/selected-album)

;;group chat
(rf/reg-root-key-sub :group-chat/selected-participants :group-chat/selected-participants)
(rf/reg-root-key-sub :group-chat/deselected-members :group-chat/deselected-members)
(rf/reg-root-key-sub :group-chat/manage-members-error :group-chat/manage-members-error)

;;messages
(rf/reg-root-key-sub :messages/messages :messages)
(rf/reg-root-key-sub :messages/reactions :reactions)
(rf/reg-root-key-sub :messages/message-lists :message-lists)
(rf/reg-root-key-sub :messages/pagination-info :pagination-info)
(rf/reg-root-key-sub :messages/pin-message-lists :pin-message-lists)
(rf/reg-root-key-sub :messages/pin-messages :pin-messages)
(rf/reg-root-key-sub :messages/pin-modal :pin-modal)

(rf/reg-root-key-sub :messages-home/selected-tab :messages-home/selected-tab)

;;communities
(rf/reg-root-key-sub :communities :communities)
(rf/reg-root-key-sub :communities/create :communities/create)
(rf/reg-root-key-sub :communities/create-channel :communities/create-channel)
(rf/reg-root-key-sub :communities/requests-to-join :communities/requests-to-join)
(rf/reg-root-key-sub :communities/community-id-input :communities/community-id-input)
(rf/reg-root-key-sub :communities/fetching-communities :communities/fetching-communities)
(rf/reg-root-key-sub :communities/my-pending-requests-to-join :communities/my-pending-requests-to-join)
(rf/reg-root-key-sub :communities/collapsed-categories :communities/collapsed-categories)
(rf/reg-root-key-sub :communities/selected-tab :communities/selected-tab)
(rf/reg-root-key-sub :contract-communities :contract-communities)
(rf/reg-root-key-sub :communities/permissioned-balances :communities/permissioned-balances)
(rf/reg-root-key-sub :communities/permissions-check :communities/permissions-check)
(rf/reg-root-key-sub :communities/permissions-check-all :communities/permissions-check-all)
(rf/reg-root-key-sub :communities/all-addresses-to-reveal :communities/all-addresses-to-reveal)
(rf/reg-root-key-sub :communities/all-airdrop-addresses :communities/all-airdrop-addresses)
(rf/reg-root-key-sub :communities/selected-share-all-addresses :communities/selected-share-all-addresses)
(rf/reg-root-key-sub :communities/permissions-checks-for-selection
                     :communities/permissions-checks-for-selection)
(rf/reg-root-key-sub :communities/channel-permissions-check :communities/channel-permissions-check)
(rf/reg-root-key-sub :communities/join-requests-for-signing :communities/join-requests-for-signing)

;;activity center
(rf/reg-root-key-sub :activity-center :activity-center)

;;wallet
(rf/reg-root-key-sub :wallet :wallet)

;;wallet-connect
(rf/reg-root-key-sub :wallet-connect/web3-wallet :wallet-connect/web3-wallet)
(rf/reg-root-key-sub :wallet-connect/current-proposal :wallet-connect/current-proposal)
(rf/reg-root-key-sub :wallet-connect/current-request :wallet-connect/current-request)
(rf/reg-root-key-sub :wallet-connect/sessions :wallet-connect/sessions)

;;biometrics
(rf/reg-root-key-sub :biometrics :biometrics)

;;settings
(rf/reg-root-key-sub :settings/change-password :settings/change-password)

;;debug
(when js/goog.DEBUG
  (rf/reg-root-key-sub :dev/previewed-component :dev/previewed-component))

;;theme
(rf/reg-root-key-sub :theme :theme)

;; centralized-metrics
(rf/reg-root-key-sub :centralized-metrics/enabled? :centralized-metrics/enabled?)
(rf/reg-root-key-sub :centralized-metrics/user-confirmed? :centralized-metrics/user-confirmed?)
(rf/reg-root-key-sub :centralized-metrics/user-id :centralized-metrics/user-id)

;;keycard
(rf/reg-root-key-sub :keycard :keycard)

(rf/reg-root-key-sub :log-level/pre-login-log-level :log-level/pre-login-log-level)

;; domain.market
