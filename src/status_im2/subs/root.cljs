(ns status-im2.subs.root
  (:require
    [re-frame.core :as re-frame]
    status-im2.subs.activity-center
    status-im2.subs.bootnodes
    status-im2.subs.browser
    status-im2.subs.chat.chats
    status-im2.subs.chat.messages
    status-im2.subs.communities
    status-im2.subs.contact
    status-im2.subs.ens
    status-im2.subs.general
    status-im2.subs.home
    status-im2.subs.keycard
    status-im2.subs.mailservers
    status-im2.subs.networks
    status-im2.subs.onboarding
    status-im2.subs.pairing
    status-im2.subs.profile
    status-im2.subs.search
    status-im2.subs.shell
    status-im2.subs.stickers
    status-im2.subs.toasts
    status-im2.subs.wallet.signing
    status-im2.subs.wallet.transactions
    status-im2.subs.wallet.wallet))

(defn reg-root-key-sub
  [sub-name db-key]
  (re-frame/reg-sub sub-name (fn [db] (get db db-key))))

;;view
(reg-root-key-sub :view-id :view-id)
(reg-root-key-sub :screen-params :navigation/screen-params)
(reg-root-key-sub :shared-element-id :shared-element-id)


;;bottom sheet old
(reg-root-key-sub :bottom-sheet/show? :bottom-sheet/show?)
(reg-root-key-sub :bottom-sheet/view :bottom-sheet/view)
(reg-root-key-sub :bottom-sheet/options :bottom-sheet/options)

;;bottom sheet
(reg-root-key-sub :bottom-sheet :bottom-sheet)

;;general
(reg-root-key-sub :syncing :syncing)
(reg-root-key-sub :sync-state :sync-state)
(reg-root-key-sub :network-status :network-status)
(reg-root-key-sub :peers-count :peers-count)
(reg-root-key-sub :about-app/node-info :node-info)
(reg-root-key-sub :peers-summary :peers-summary)
(reg-root-key-sub :dimensions/window :dimensions/window)
(reg-root-key-sub :fleets/custom-fleets :custom-fleets)
(reg-root-key-sub :animations :animations)
(reg-root-key-sub :ui/search :ui/search)
(reg-root-key-sub :web3-node-version :web3-node-version)
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
(reg-root-key-sub :visibility-status-updates :visibility-status-updates)
(reg-root-key-sub :shell/switcher-cards :shell/switcher-cards)
(reg-root-key-sub :password-authentication :password-authentication)
(reg-root-key-sub :shell/floating-screens :shell/floating-screens)
(reg-root-key-sub :shell/loaded-screens :shell/loaded-screens)
(reg-root-key-sub :initials-avatar-font-file :initials-avatar-font-file)

;;NOTE this one is not related to ethereum network
;; it is about cellular network/ wifi network
(reg-root-key-sub :network/type :network/type)

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

;;chat
(reg-root-key-sub :chats/cooldown-enabled? :chat/cooldown-enabled?)
(reg-root-key-sub :chats/chats :chats)
(reg-root-key-sub :chats/current-chat-id :current-chat-id)
(reg-root-key-sub :public-group-topic :public-group-topic)
(reg-root-key-sub :chats/loading? :chats/loading?)
(reg-root-key-sub :new-chat-name :new-chat-name)
(reg-root-key-sub :group-chat-profile/editing? :group-chat-profile/editing?)
(reg-root-key-sub :group-chat-profile/profile :group-chat-profile/profile)
(reg-root-key-sub :group-chat/selected-participants :group-chat/selected-participants)
(reg-root-key-sub :group-chat/deselected-members :group-chat/deselected-members)
(reg-root-key-sub :chat/inputs :chat/inputs)
(reg-root-key-sub :chat/memberships :chat/memberships)
(reg-root-key-sub :camera-roll/photos :camera-roll/photos)
(reg-root-key-sub :camera-roll/end-cursor :camera-roll/end-cursor)
(reg-root-key-sub :camera-roll/has-next-page :camera-roll/has-next-page)
(reg-root-key-sub :camera-roll/loading-more :camera-roll/loading-more)
(reg-root-key-sub :camera-roll/albums :camera-roll/albums)
(reg-root-key-sub :camera-roll/selected-album :camera-roll/selected-album)
(reg-root-key-sub :group-chat/invitations :group-chat/invitations)
(reg-root-key-sub :chats/mention-suggestions :chats/mention-suggestions)
(reg-root-key-sub :chat/inputs-with-mentions :chat/inputs-with-mentions)
(reg-root-key-sub :chats-home-list :chats-home-list)
(reg-root-key-sub :chats/recording? :chats/recording?)
(reg-root-key-sub :chat/reactions-authors :chat/reactions-authors)

;;lightbox
(reg-root-key-sub :lightbox/exit-signal :lightbox/exit-signal)
(reg-root-key-sub :lightbox/zoom-out-signal :lightbox/zoom-out-signal)
(reg-root-key-sub :lightbox/orientation :lightbox/orientation)
(reg-root-key-sub :lightbox/scale :lightbox/scale)

;;messages
(reg-root-key-sub :messages/messages :messages)
(reg-root-key-sub :messages/reactions :reactions)
(reg-root-key-sub :messages/message-lists :message-lists)
(reg-root-key-sub :messages/pagination-info :pagination-info)
(reg-root-key-sub :messages/pin-message-lists :pin-message-lists)
(reg-root-key-sub :messages/pin-messages :pin-messages)
(reg-root-key-sub :messages/pin-modal :pin-modal)

;;browser
(reg-root-key-sub :browsers :browser/browsers)
(reg-root-key-sub :browser/options :browser/options)
(reg-root-key-sub :dapps/permissions :dapps/permissions)
(reg-root-key-sub :bookmarks :bookmarks/bookmarks)
(reg-root-key-sub :browser/screen-id :browser/screen-id)

;;stickers
(reg-root-key-sub :stickers/selected-pack :stickers/selected-pack)
(reg-root-key-sub :stickers/packs :stickers/packs)
(reg-root-key-sub :stickers/recent-stickers :stickers/recent-stickers)

;;mailserver
(reg-root-key-sub :mailserver/current-id :mailserver/current-id)
(reg-root-key-sub :mailserver/mailservers :mailserver/mailservers)
(reg-root-key-sub :mailserver.edit/mailserver :mailserver.edit/mailserver)
(reg-root-key-sub :mailserver/state :mailserver/state)
(reg-root-key-sub :mailserver/pending-requests :mailserver/pending-requests)
(reg-root-key-sub :mailserver/request-error? :mailserver/request-error)
(reg-root-key-sub :mailserver/fetching-gaps-in-progress :mailserver/fetching-gaps-in-progress)

;;contacts
(reg-root-key-sub :contacts/contacts-raw :contacts/contacts)
(reg-root-key-sub :contacts/current-contact-identity :contacts/identity)
(reg-root-key-sub :contacts/current-contact-ens-name :contacts/ens-name)
(reg-root-key-sub :contacts/new-identity :contacts/new-identity)
(reg-root-key-sub :group/selected-contacts :group/selected-contacts)
(reg-root-key-sub :contacts/search-query :contacts/search-query)

;;wallet
(reg-root-key-sub :wallet :wallet)
(reg-root-key-sub :prices :prices)
(reg-root-key-sub :prices-loading? :prices-loading?)
(reg-root-key-sub :wallet.transactions :wallet.transactions)
(reg-root-key-sub :wallet/custom-token-screen :wallet/custom-token-screen)
(reg-root-key-sub :wallet/prepare-transaction :wallet/prepare-transaction)
(reg-root-key-sub :wallet-service/manual-setting :wallet-service/manual-setting)
(reg-root-key-sub :wallet/recipient :wallet/recipient)
(reg-root-key-sub :wallet/favourites :wallet/favourites)
(reg-root-key-sub :wallet/refreshing-history? :wallet/refreshing-history?)
(reg-root-key-sub :wallet/fetching-error :wallet/fetching-error)
(reg-root-key-sub :wallet/non-archival-node :wallet/non-archival-node)
(reg-root-key-sub :wallet/current-base-fee :wallet/current-base-fee)
(reg-root-key-sub :wallet/slow-base-fee :wallet/slow-base-fee)
(reg-root-key-sub :wallet/normal-base-fee :wallet/normal-base-fee)
(reg-root-key-sub :wallet/fast-base-fee :wallet/fast-base-fee)
(reg-root-key-sub :wallet/current-priority-fee :wallet/current-priority-fee)
(reg-root-key-sub :wallet/transactions-management-enabled? :wallet/transactions-management-enabled?)
(reg-root-key-sub :wallet/all-tokens :wallet/all-tokens)
(reg-root-key-sub :wallet/collectible-collections :wallet/collectible-collections)
(reg-root-key-sub :wallet/fetching-collection-assets :wallet/fetching-collection-assets)
(reg-root-key-sub :wallet/collectible-assets :wallet/collectible-assets)
(reg-root-key-sub :wallet/selected-collectible :wallet/selected-collectible)
(reg-root-key-sub :wallet/modal-selecting-source-token? :wallet/modal-selecting-source-token?)
(reg-root-key-sub :wallet/swap-from-token :wallet/swap-from-token)
(reg-root-key-sub :wallet/swap-to-token :wallet/swap-to-token)
(reg-root-key-sub :wallet/swap-from-token-amount :wallet/swap-from-token-amount)
(reg-root-key-sub :wallet/swap-to-token-amount :wallet/swap-to-token-amount)
(reg-root-key-sub :wallet/swap-advanced-mode? :wallet/swap-advanced-mode?)

;;; Link previews

(reg-root-key-sub :link-previews-whitelist :link-previews-whitelist)
(reg-root-key-sub :chat/link-previews :chat/link-previews)

;;commands
(reg-root-key-sub :commands/select-account :commands/select-account)

;;ethereum
(reg-root-key-sub :ethereum/current-block :ethereum/current-block)

;;ens
(reg-root-key-sub :ens/registration :ens/registration)
(reg-root-key-sub :ens/registrations :ens/registrations)
(reg-root-key-sub :ens/names :ens/names)

;;signing
(reg-root-key-sub :signing/sign :signing/sign)
(reg-root-key-sub :signing/tx :signing/tx)
(reg-root-key-sub :signing/edit-fee :signing/edit-fee)

;;intro-wizard
(reg-root-key-sub :intro-wizard-state :intro-wizard)

(reg-root-key-sub :toasts :toasts)
(reg-root-key-sub :popover/popover :popover/popover)
(reg-root-key-sub :visibility-status-popover/popover :visibility-status-popover/popover)
(reg-root-key-sub :add-account :add-account)

(reg-root-key-sub :keycard :keycard)

(reg-root-key-sub :auth-method :auth-method)

;; keycard
(reg-root-key-sub :keycard/banner-hidden :keycard/banner-hidden)

;; delete profile
(reg-root-key-sub :delete-profile/error :delete-profile/error)
(reg-root-key-sub :delete-profile/keep-keys-on-keycard? :delete-profile/keep-keys-on-keycard?)

;; push notifications
(reg-root-key-sub :push-notifications/servers :push-notifications/servers)
(reg-root-key-sub :push-notifications/preferences :push-notifications/preferences)

(reg-root-key-sub :buy-crypto/on-ramps :buy-crypto/on-ramps)

;; communities

(reg-root-key-sub :communities :communities)
(reg-root-key-sub :communities/create :communities/create)
(reg-root-key-sub :communities/create-channel :communities/create-channel)
(reg-root-key-sub :communities/requests-to-join :communities/requests-to-join)
(reg-root-key-sub :communities/community-id-input :communities/community-id-input)
(reg-root-key-sub :communities/resolve-community-info :communities/resolve-community-info)
(reg-root-key-sub :communities/my-pending-requests-to-join :communities/my-pending-requests-to-join)
(reg-root-key-sub :communities/collapsed-categories :communities/collapsed-categories)
(reg-root-key-sub :communities/selected-tab :communities/selected-tab)

(reg-root-key-sub :contract-communities :contract-communities)

(reg-root-key-sub :activity-center :activity-center)

(reg-root-key-sub :bug-report/description-error :bug-report/description-error)
(reg-root-key-sub :bug-report/details :bug-report/details)

(reg-root-key-sub :backup/performing-backup :backup/performing-backup)


;; wallet connect
(reg-root-key-sub :wallet-connect/proposal-metadata :wallet-connect/proposal-metadata)
(reg-root-key-sub :wallet-connect/enabled? :wallet-connect/enabled?)
(reg-root-key-sub :wallet-connect/session-connected :wallet-connect/session-connected)
(reg-root-key-sub :wallet-connect/showing-app-management-sheet?
                  :wallet-connect/showing-app-management-sheet?)
(reg-root-key-sub :wallet-connect/sessions :wallet-connect/sessions)
(reg-root-key-sub :wallet-connect/session-managed :wallet-connect/session-managed)
(reg-root-key-sub :contact-requests/pending :contact-requests/pending)

; media-server
(reg-root-key-sub :mediaserver/port :mediaserver/port)

; onboarding
(reg-root-key-sub :onboarding-2/generated-keys? :onboarding-2/generated-keys?)
(reg-root-key-sub :onboarding-2/new-account? :onboarding-2/new-account?)
(reg-root-key-sub :onboarding-2/profile :onboarding-2/profile)

; Testing

(reg-root-key-sub :messenger/started? :messenger/started?)

; Messages home view -> tabs
(reg-root-key-sub :messages-home/selected-tab :messages-home/selected-tab)
