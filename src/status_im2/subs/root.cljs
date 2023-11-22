(ns status-im2.subs.root
  (:require
    [re-frame.core :as re-frame]
    status-im2.subs.activity-center
    status-im2.subs.chats
    status-im2.subs.communities
    status-im2.subs.contact
    status-im2.subs.general
    status-im2.subs.messages
    status-im2.subs.onboarding
    status-im2.subs.pairing
    status-im2.subs.profile
    status-im2.subs.shell
    status-im2.subs.wallet.collectibles
    status-im2.subs.wallet.networks
    status-im2.subs.wallet.send
    status-im2.subs.wallet.wallet))

(defn reg-root-key-sub
  [sub-name db-key]
  (re-frame/reg-sub sub-name (fn [db] (get db db-key))))

;;view
(reg-root-key-sub :view-id :view-id)
(reg-root-key-sub :screen-params :navigation/screen-params)
(reg-root-key-sub :shared-element-id :shared-element-id)

;;bottom sheet
(reg-root-key-sub :bottom-sheet :bottom-sheet)

;;media-server
(reg-root-key-sub :mediaserver/port :mediaserver/port)

;;push notifications
(reg-root-key-sub :push-notifications/preferences :push-notifications/preferences)

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
(reg-root-key-sub :biometric/supported-type :biometric/supported-type)
(reg-root-key-sub :app-state :app-state)
(reg-root-key-sub :home-items-show-number :home-items-show-number)
(reg-root-key-sub :waku/v2-peer-stats :peer-stats)
(reg-root-key-sub :password-authentication :password-authentication)
(reg-root-key-sub :initials-avatar-font-file :initials-avatar-font-file)

;;onboarding
(reg-root-key-sub :onboarding/generated-keys? :onboarding/generated-keys?)
(reg-root-key-sub :onboarding/new-account? :onboarding/new-account?)
(reg-root-key-sub :onboarding/profile :onboarding/profile)

;;shell
(reg-root-key-sub :shell/switcher-cards :shell/switcher-cards)
(reg-root-key-sub :shell/floating-screens :shell/floating-screens)
(reg-root-key-sub :shell/loaded-screens :shell/loaded-screens)

;;my profile
(reg-root-key-sub :my-profile/seed :my-profile/seed)
;;profiles
(reg-root-key-sub :profile/profiles-overview :profile/profiles-overview)
(reg-root-key-sub :profile/login :profile/login)
(reg-root-key-sub :profile/profile :profile/profile)
(reg-root-key-sub :profile/wallet-accounts :profile/wallet-accounts)

(reg-root-key-sub :multiaccount/reset-password-form-vals :multiaccount/reset-password-form-vals)
(reg-root-key-sub :multiaccount/reset-password-errors :multiaccount/reset-password-errors)
(reg-root-key-sub :multiaccount/resetting-password? :multiaccount/resetting-password?)
;;delete profile
(reg-root-key-sub :delete-profile/error :delete-profile/error)
(reg-root-key-sub :delete-profile/keep-keys-on-keycard? :delete-profile/keep-keys-on-keycard?)

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
(reg-root-key-sub :chat/inputs-with-mentions :chat/inputs-with-mentions)
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
(reg-root-key-sub :group-chat-profile/editing? :group-chat-profile/editing?)
(reg-root-key-sub :group-chat-profile/profile :group-chat-profile/profile)
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
(reg-root-key-sub :communities/channels-permissions :community-channels-permissions)
(reg-root-key-sub :communities/requests-to-join :communities/requests-to-join)
(reg-root-key-sub :communities/community-id-input :communities/community-id-input)
(reg-root-key-sub :communities/resolve-community-info :communities/resolve-community-info)
(reg-root-key-sub :communities/my-pending-requests-to-join :communities/my-pending-requests-to-join)
(reg-root-key-sub :communities/collapsed-categories :communities/collapsed-categories)
(reg-root-key-sub :communities/selected-tab :communities/selected-tab)
(reg-root-key-sub :contract-communities :contract-communities)

;;activity center
(reg-root-key-sub :activity-center :activity-center)

;;wallet
(reg-root-key-sub :wallet :wallet)
(reg-root-key-sub :wallet/scanned-address :wallet/scanned-address)
(reg-root-key-sub :wallet/create-account :wallet/create-account)
(reg-root-key-sub :wallet/networks :wallet/networks)
(reg-root-key-sub :wallet/local-suggestions :wallet/local-suggestions)
(reg-root-key-sub :wallet/valid-ens-or-address? :wallet/valid-ens-or-address?)

;;debug
(when js/goog.DEBUG
  (reg-root-key-sub :dev/previewed-component :dev/previewed-component))
