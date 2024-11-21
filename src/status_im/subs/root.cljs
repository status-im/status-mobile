(ns status-im.subs.root
  (:require
    [re-frame.core :as re-frame]
    status-im.subs.activity-center
    status-im.subs.alert-banner
    status-im.subs.biometrics
    status-im.subs.bottom-sheet
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
    status-im.subs.wallet.activities
    status-im.subs.wallet.buy
    status-im.subs.wallet.collectibles
    status-im.subs.wallet.dapps.core
    status-im.subs.wallet.networks
    status-im.subs.wallet.saved-addresses
    status-im.subs.wallet.send
    status-im.subs.wallet.swap
    status-im.subs.wallet.wallet))

(defn reg-root-key-sub
  [sub-name db-key]
  (re-frame/reg-sub sub-name (fn [db] (get db db-key))))

;;view
(reg-root-key-sub :view-id :view-id)
(reg-root-key-sub :screen-params :navigation/screen-params)
(reg-root-key-sub :animation-shared-element-id :animation-shared-element-id)

;;bottom sheet
(reg-root-key-sub :bottom-sheet :bottom-sheet)

;;media-server
(reg-root-key-sub :mediaserver/port :mediaserver/port)

;;push notifications
(reg-root-key-sub :push-notifications/preferences :push-notifications/preferences)

;;device
(reg-root-key-sub :network/status :network/status)
(reg-root-key-sub :network/type :network/type)

;;general
(reg-root-key-sub :messenger/started? :messenger/started?)
(reg-root-key-sub :animations :animations)
(reg-root-key-sub :toasts :toasts)
(reg-root-key-sub :popover/popover :popover/popover)
(reg-root-key-sub :auth-method :auth-method)
(reg-root-key-sub :syncing :syncing)
(reg-root-key-sub :sync-state :sync-state)
(reg-root-key-sub :dimensions/window :dimensions/window)
(reg-root-key-sub :sync-data :sync-data)
(reg-root-key-sub :mobile-network/remember-choice? :mobile-network/remember-choice?)
(reg-root-key-sub :qr-modal :qr-modal)
(reg-root-key-sub :bootnodes/manage :bootnodes/manage)
(reg-root-key-sub :wakuv2-nodes/manage :wakuv2-nodes/manage)
(reg-root-key-sub :wakuv2-nodes/list :wakuv2-nodes/list)
(reg-root-key-sub :networks/current-network :networks/current-network)
(reg-root-key-sub :networks/networks :networks/networks)
(reg-root-key-sub :networks/manage :networks/manage)
(reg-root-key-sub :get-pairing-installations :pairing/installations)
(reg-root-key-sub :tooltips :tooltips)
(reg-root-key-sub :app-state :app-state)
(reg-root-key-sub :home-items-show-number :home-items-show-number)
(reg-root-key-sub :password-authentication :password-authentication)
(reg-root-key-sub :initials-avatar-font-file :initials-avatar-font-file)
(reg-root-key-sub :alert-banners :alert-banners)
(reg-root-key-sub :alert-banners/hide? :alert-banners/hide?)
(reg-root-key-sub :currencies :currencies)

;;onboarding
(reg-root-key-sub :onboarding/generated-keys? :onboarding/generated-keys?)
(reg-root-key-sub :onboarding/new-account? :onboarding/new-account?)
(reg-root-key-sub :onboarding/profile :onboarding/profile)

;;my profile
(reg-root-key-sub :my-profile/seed :my-profile/seed)
;;profiles
(reg-root-key-sub :profile/profiles-overview :profile/profiles-overview)
(reg-root-key-sub :profile/login :profile/login)
(reg-root-key-sub :profile/profile :profile/profile)
(reg-root-key-sub :profile/logging-out? :profile/logging-out?)
(reg-root-key-sub :profile/wallet-accounts :profile/wallet-accounts)

(reg-root-key-sub :multiaccount/reset-password-form-vals :multiaccount/reset-password-form-vals)
(reg-root-key-sub :multiaccount/reset-password-errors :multiaccount/reset-password-errors)
(reg-root-key-sub :multiaccount/resetting-password? :multiaccount/resetting-password?)

;;chat
(reg-root-key-sub :chats/cooldown-enabled? :chat/cooldown-enabled?)
(reg-root-key-sub :chats/chats :chats)
(reg-root-key-sub :chats/current-chat-id :current-chat-id)
(reg-root-key-sub :public-group-topic :public-group-topic)
(reg-root-key-sub :chats/loading? :chats/loading?)
(reg-root-key-sub :new-chat-name :new-chat-name)
(reg-root-key-sub :chat/inputs :chat/inputs)
(reg-root-key-sub :chat/memberships :chat/memberships)
(reg-root-key-sub :group-chat/invitations :group-chat/invitations)
(reg-root-key-sub :chats/mention-suggestions :chats/mention-suggestions)

(reg-root-key-sub :chats-home-list :chats-home-list)
(reg-root-key-sub :chats/recording? :chats/recording?)
(reg-root-key-sub :reactions/authors :reactions/authors)

;;chat images lightbox
(reg-root-key-sub :lightbox/exit-signal :lightbox/exit-signal)
(reg-root-key-sub :lightbox/zoom-out-signal :lightbox/zoom-out-signal)
(reg-root-key-sub :lightbox/orientation :lightbox/orientation)
(reg-root-key-sub :lightbox/scale :lightbox/scale)

;;chat images camera roll
(reg-root-key-sub :camera-roll/photos :camera-roll/photos)
(reg-root-key-sub :camera-roll/end-cursor :camera-roll/end-cursor)
(reg-root-key-sub :camera-roll/has-next-page :camera-roll/has-next-page)
(reg-root-key-sub :camera-roll/loading-more :camera-roll/loading-more)
(reg-root-key-sub :camera-roll/albums :camera-roll/albums)
(reg-root-key-sub :camera-roll/selected-album :camera-roll/selected-album)

;;group chat
(reg-root-key-sub :group-chat/selected-participants :group-chat/selected-participants)
(reg-root-key-sub :group-chat/deselected-members :group-chat/deselected-members)

;;messages
(reg-root-key-sub :messages/messages :messages)
(reg-root-key-sub :messages/reactions :reactions)
(reg-root-key-sub :messages/message-lists :message-lists)
(reg-root-key-sub :messages/pagination-info :pagination-info)
(reg-root-key-sub :messages/pin-message-lists :pin-message-lists)
(reg-root-key-sub :messages/pin-messages :pin-messages)
(reg-root-key-sub :messages/pin-modal :pin-modal)

(reg-root-key-sub :messages-home/selected-tab :messages-home/selected-tab)

;;communities
(reg-root-key-sub :communities :communities)
(reg-root-key-sub :communities/create :communities/create)
(reg-root-key-sub :communities/create-channel :communities/create-channel)
(reg-root-key-sub :communities/requests-to-join :communities/requests-to-join)
(reg-root-key-sub :communities/community-id-input :communities/community-id-input)
(reg-root-key-sub :communities/fetching-communities :communities/fetching-communities)
(reg-root-key-sub :communities/my-pending-requests-to-join :communities/my-pending-requests-to-join)
(reg-root-key-sub :communities/collapsed-categories :communities/collapsed-categories)
(reg-root-key-sub :communities/selected-tab :communities/selected-tab)
(reg-root-key-sub :contract-communities :contract-communities)
(reg-root-key-sub :communities/permissioned-balances :communities/permissioned-balances)
(reg-root-key-sub :communities/permissions-check :communities/permissions-check)
(reg-root-key-sub :communities/permissions-check-all :communities/permissions-check-all)
(reg-root-key-sub :communities/all-addresses-to-reveal :communities/all-addresses-to-reveal)
(reg-root-key-sub :communities/all-airdrop-addresses :communities/all-airdrop-addresses)
(reg-root-key-sub :communities/selected-share-all-addresses :communities/selected-share-all-addresses)
(reg-root-key-sub :communities/permissions-checks-for-selection
                  :communities/permissions-checks-for-selection)
(reg-root-key-sub :communities/channel-permissions-check :communities/channel-permissions-check)

;;activity center
(reg-root-key-sub :activity-center :activity-center)

;;wallet
(reg-root-key-sub :wallet :wallet)

;;wallet-connect
(reg-root-key-sub :wallet-connect/web3-wallet :wallet-connect/web3-wallet)
(reg-root-key-sub :wallet-connect/current-proposal :wallet-connect/current-proposal)
(reg-root-key-sub :wallet-connect/current-request :wallet-connect/current-request)
(reg-root-key-sub :wallet-connect/sessions :wallet-connect/sessions)

;;biometrics
(reg-root-key-sub :biometrics :biometrics)

;;settings
(reg-root-key-sub :settings/change-password :settings/change-password)

;;debug
(when js/goog.DEBUG
  (reg-root-key-sub :dev/previewed-component :dev/previewed-component))

;;theme
(reg-root-key-sub :theme :theme)

;; centralized-metrics
(reg-root-key-sub :centralized-metrics/enabled? :centralized-metrics/enabled?)
(reg-root-key-sub :centralized-metrics/user-confirmed? :centralized-metrics/user-confirmed?)

;;keycard
(reg-root-key-sub :keycard :keycard)
