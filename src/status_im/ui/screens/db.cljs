(ns status-im.ui.screens.db
  (:require [cljs.spec.alpha :as spec]
            [status-im.constants :as constants]
            [status-im.utils.dimensions :as dimensions]
            [status-im.fleet.core :as fleet]
            status-im.transport.db
            status-im.multiaccounts.db
            status-im.contact.db
            status-im.ui.screens.group.db
            status-im.chat.specs
            status-im.ui.screens.profile.db
            status-im.network.module
            status-im.mailserver.db
            status-im.ens.db
            status-im.browser.db
            status-im.ui.screens.add-new.new-public-chat.db
            status-im.ui.components.bottom-sheet.core
            status-im.ui.screens.intro.db
            [status-im.wallet.db :as wallet.db]))

;; initial state of app-db
(def app-db {:keyboard-height                    0
             :contacts/contacts                  {}
             :pairing/installations              {}
             :qr-codes                           {}
             :group/selected-contacts            #{}
             :chats                              {}
             :current-chat-id                    nil
             :selected-participants              #{}
             :sync-state                         :done
             :app-state                          "active"
             :wallet                              wallet.db/default-wallet
             :wallet/all-tokens                  {}
             :prices                             {}
             :peers-count                        0
             :node-info                          {}
             :peers-summary                      []
             :my-profile/editing?                false
             :transport/filters                  {}
             :transport/message-envelopes        {}
             :mailserver/mailservers             (fleet/default-mailservers {})
             :mailserver/topics                  {}
             :mailserver/pending-requests        0
             :chat/cooldowns                     0
             :chat/cooldown-enabled?             false
             :chat/last-outgoing-message-sent-at 0
             :chat/spam-messages-frequency       0
             :tooltips                           {}
             :initial-props                      {}
             :desktop/desktop                    {:tab-view-id :home}
             :dimensions/window                  (dimensions/window)
             :registry                           {}
             :stickers/packs-owned               #{}
             :stickers/packs-pending             #{}
             :hardwallet                         {:nfc-enabled?   false
                                                  :pin            {:original     []
                                                                   :confirmation []
                                                                   :current      []
                                                                   :puk          []
                                                                   :enter-step   :original}}
             :two-pane-ui-enabled?               (dimensions/fit-two-pane?)})

;;;;GLOBAL

;;"http://localhost:8545"
(spec/def ::rpc-url (spec/nilable string?))
;;object? doesn't work
(spec/def ::web3 (spec/nilable any?))
(spec/def ::web3-node-version (spec/nilable string?))
;;object?

;;height of native keyboard if shown
(spec/def ::keyboard-height (spec/nilable number?))
(spec/def ::keyboard-max-height (spec/nilable number?))
;;:online - presence of internet connection in the phone
(spec/def ::network-status (spec/nilable keyword?))
;; ui connectivity status
(spec/def :connectivity/ui-status-properties (spec/nilable map?))

(spec/def ::app-state string?)
(spec/def ::app-in-background-since (spec/nilable number?))
(spec/def ::app-active-since (spec/nilable number?))
(spec/def ::logged-in-since (spec/nilable number?))

;;;;NODE

(spec/def ::sync-state (spec/nilable #{:pending :in-progress :synced :done :offline}))
(spec/def ::sync-data (spec/nilable map?))

;;;;NAVIGATION

;;current view
(spec/def :navigation/view-id (spec/nilable keyword?))
;; navigation screen params
(spec/def :navigation.screen-params/network-details (spec/keys :req [:networks/selected-network]))
(spec/def :navigation.screen-params/browser (spec/nilable map?))
(spec/def :navigation.screen-params.profile-qr-viewer/contact (spec/nilable map?))
(spec/def :navigation.screen-params.profile-qr-viewer/source (spec/nilable keyword?))
(spec/def :navigation.screen-params.profile-qr-viewer/value (spec/nilable string?))
(spec/def :navigation.screen-params/profile-qr-viewer (spec/keys :opt-un [:navigation.screen-params.profile-qr-viewer/contact
                                                                          :navigation.screen-params.profile-qr-viewer/source
                                                                          :navigation.screen-params.profile-qr-viewer/value]))
(spec/def :navigation.screen-params.qr-scanner/current-qr-context (spec/nilable any?))
(spec/def :navigation.screen-params/qr-scanner (spec/keys :opt-un [:navigation.screen-params.qr-scanner/current-qr-context]))
(spec/def :navigation.screen-params.group-contacts/show-search? (spec/nilable any?))
(spec/def :navigation.screen-params/group-contacts (spec/keys :opt [:group/contact-group-id]
                                                              :opt-un [:navigation.screen-params.group-contacts/show-search?]))
(spec/def :navigation.screen-params.edit-contact-group/group (spec/nilable any?))
(spec/def :navigation.screen-params.edit-contact-group/group-type (spec/nilable any?))
(spec/def :navigation.screen-params/edit-contact-group (spec/keys :opt-un [:navigation.screen-params.edit-contact-group/group
                                                                           :navigation.screen-params.edit-contact-group/group-type]))

(spec/def :navigation.screen-params/collectibles-list map?)

(spec/def :navigation.screen-params/manage-dapps-permissions map?)

(spec/def :navigation/screen-params (spec/nilable (spec/keys :opt-un [:navigation.screen-params/network-details
                                                                      :navigation.screen-params/browser
                                                                      :navigation.screen-params/profile-qr-viewer
                                                                      :navigation.screen-params/qr-scanner
                                                                      :navigation.screen-params/group-contacts
                                                                      :navigation.screen-params/edit-contact-group
                                                                      :navigation.screen-params/collectibles-list
                                                                      :navigation.screen-params/manage-dapps-permissions])))

(spec/def :desktop/desktop (spec/nilable any?))
(spec/def ::tooltips (spec/nilable any?))
(spec/def ::initial-props (spec/nilable any?))
(spec/def ::two-pane-ui-enabled? (spec/nilable boolean?))

;;;;NETWORK

(spec/def ::network (spec/nilable string?))
(spec/def ::chain (spec/nilable string?))
(spec/def ::peers-count (spec/nilable integer?))
(spec/def ::node-info (spec/nilable map?))
(spec/def ::peers-summary (spec/nilable vector?))

(spec/def ::collectible (spec/nilable map?))
(spec/def ::collectibles (spec/nilable map?))

;;;;NODE

(spec/def ::message-envelopes (spec/nilable map?))

;;;; Supported Biometric authentication types

(spec/def ::supported-biometric-auth (spec/nilable #{:FaceID :TouchID :fingerprint}))

;;;;UNIVERSAL LINKS

(spec/def :universal-links/url (spec/nilable string?))

;; DIMENSIONS
(spec/def :dimensions/window map?)

(spec/def ::hardwallet (spec/nilable map?))

(spec/def :stickers/packs (spec/nilable map?))
(spec/def :stickers/packs-owned (spec/nilable set?))
(spec/def :stickers/packs-pending (spec/nilable set?))
(spec/def :stickers/packs-installed (spec/nilable map?))
(spec/def :stickers/selected-pack (spec/nilable any?))
(spec/def :stickers/recent (spec/nilable vector?))
(spec/def :wallet/custom-token-screen (spec/nilable map?))

(spec/def :signing/in-progress? (spec/nilable boolean?))
(spec/def :signing/queue (spec/nilable any?))
(spec/def :signing/tx (spec/nilable map?))
(spec/def :signing/sign (spec/nilable map?))
(spec/def :signing/edit-fee (spec/nilable map?))

(spec/def :popover/popover (spec/nilable map?))

(spec/def :wallet/prepare-transaction (spec/nilable map?))

(spec/def ::db (spec/keys :opt [:contacts/contacts
                                :contacts/new-identity
                                :contacts/identity
                                :contacts/ui-props
                                :contacts/list-ui-props
                                :contacts/click-handler
                                :contacts/click-action
                                :contacts/click-params
                                :pairing/installations
                                :group/selected-contacts
                                :multiaccounts/multiaccounts
                                :multiaccounts/recover
                                :multiaccounts/login
                                :multiaccount/accounts
                                :my-profile/profile
                                :my-profile/default-name
                                :my-profile/editing?
                                :my-profile/advanced?
                                :my-profile/seed
                                :group-chat-profile/profile
                                :group-chat-profile/editing?
                                :networks/selected-network
                                :networks/networks
                                :networks/manage
                                :bootnodes/manage
                                :universal-links/url
                                :browser/browsers
                                :browser/options
                                :navigation/screen-params
                                :chat/cooldowns
                                :chat/cooldown-enabled?
                                :chat/last-outgoing-message-sent-at
                                :chat/spam-messages-frequency
                                :transport/message-envelopes
                                :transport/filters
                                :mailserver.edit/mailserver
                                :mailserver/mailservers
                                :mailserver/current-id
                                :mailserver/state
                                :mailserver/topics
                                :mailserver/pending-requests
                                :mailserver/current-request
                                :mailserver/connection-checks
                                :mailserver/request-to
                                :mailserver/request-error
                                :desktop/desktop
                                :dimensions/window
                                :dapps/permissions
                                :wallet/all-tokens
                                :ui/contact
                                :ui/search
                                :ui/chat
                                :chats/loading?
                                :stickers/packs
                                :stickers/packs-installed
                                :stickers/selected-pack
                                :stickers/recent
                                :stickers/packs-owned
                                :stickers/packs-pending
                                :bottom-sheet/show?
                                :bottom-sheet/view
                                :bottom-sheet/options
                                :wallet/custom-token-screen
                                :wallet/prepare-transaction
                                :signing/in-progress?
                                :signing/queue
                                :signing/sign
                                :signing/tx
                                :signing/edit-fee
                                :popover/popover
                                :wallet/sign-phrase-showed?]
                          :opt-un [::modal
                                   ::rpc-url
                                   ::tooltips
                                   ::initial-props
                                   ::web3
                                   ::web3-node-version
                                   ::keyboard-height
                                   ::keyboard-max-height
                                   ::network-status
                                   ::peers-count
                                   ::node-info
                                   ::peers-summary
                                   ::sync-state
                                   ::sync-data
                                   ::network
                                   ::chain
                                   ::app-state
                                   ::app-in-background-since
                                   ::app-active-since
                                   ::logged-in-since
                                   ::hardwallet
                                   ::auth-method
                                   :multiaccount/multiaccount
                                   :navigation/view-id
                                   :chat/chats
                                   :chat/current-chat-id
                                   :chat/chat-id
                                   :chat/new-chat
                                   :chat/new-chat-name
                                   :chat/animations
                                   :chat/chat-ui-props
                                   :chat/chat-list-ui-props
                                   :chat/layout-height
                                   :chat/message-data
                                   :chat/message-status
                                   :chat/selected-participants
                                   :chat/public-group-topic
                                   :chat/public-group-topic-error
                                   :chat/messages
                                   :chat/message-statuses
                                   :chat/last-clock-value
                                   :chat/loaded-chats
                                   :chat/bot-db
                                   :connectivity/ui-status-properties
                                   :ens/registration
                                   :wallet/wallet
                                   :prices/prices
                                   :prices/prices-loading?
                                   ::supported-biometric-auth
                                   ::collectible
                                   ::collectibles
                                   :registry/registry
                                   ::two-pane-ui-enabled?
                                   ::add-account
                                   :intro-wizard/intro-wizard]))
