(ns status-im.ui.screens.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as spec]
            [status-im.constants :as constants]
            [status-im.utils.platform :as platform]
            status-im.transport.db
            status-im.ui.screens.accounts.db
            status-im.ui.screens.contacts.db
            status-im.ui.screens.qr-scanner.db
            status-im.ui.screens.group.db
            status-im.chat.specs
            status-im.commands.specs
            status-im.ui.screens.profile.db
            status-im.ui.screens.network-settings.db
            status-im.ui.screens.offline-messaging-settings.db
            status-im.ui.screens.browser.db
            status-im.ui.screens.add-new.db
            status-im.ui.screens.add-new.new-public-chat.db))

;; initial state of app-db
(def app-db {:current-public-key          nil
             :status-module-initialized?  (or platform/ios? js/goog.DEBUG)
             :keyboard-height             0
             :tab-bar-visible?            true
             :navigation-stack            '()
             :contacts/contacts           {}
             :qr-codes                    {}
             :group/contact-groups        {}
             :group/selected-contacts     #{}
             :chats                       {}
             :current-chat-id             nil
             :selected-participants       #{}
             :discoveries                 {}
             :discover-search-tags        #{}
             :discover-current-dapp       {}
             :tags                        []
             :sync-state                  :done
             :wallet.transactions         constants/default-wallet-transactions
             :wallet-selected-asset       {}
             :prices                      {}
             :notifications               {}
             :network                     constants/default-network
             :networks/networks           constants/default-networks
             :inbox/wnodes                constants/default-wnodes
             :inbox/password              constants/inbox-password
             :my-profile/editing?         false
             :transport/chats             {}
             :transport/message-envelopes {}
             :desktop/desktop             {:tab-view-id :home}})

;;;;GLOBAL

;;public key of current logged in account
(spec/def ::current-public-key (spec/nilable string?))
(spec/def ::was-modal? (spec/nilable boolean?))
;;"http://localhost:8545"
(spec/def ::rpc-url (spec/nilable string?))
;;object? doesn't work
(spec/def ::web3 (spec/nilable any?))
;;object?
(spec/def ::webview-bridge (spec/nilable any?))
(spec/def ::status-module-initialized? (spec/nilable boolean?))
(spec/def ::status-node-started? (spec/nilable boolean?))
;;height of native keyboard if shown
(spec/def ::keyboard-height (spec/nilable number?))
(spec/def ::keyboard-max-height (spec/nilable number?))
(spec/def ::tab-bar-visible? (spec/nilable boolean?))
;;:online - presence of internet connection in the phone
(spec/def ::network-status (spec/nilable keyword?))

(spec/def ::mailserver-status (spec/nilable keyword?))
(spec/def ::peers-count (spec/nilable integer?))

;;;;NODE

(spec/def ::sync-listening-started (spec/nilable boolean?))
(spec/def ::sync-state (spec/nilable #{:pending :in-progress :synced :done :offline}))
(spec/def ::sync-data (spec/nilable map?))

;;;;NAVIGATION

;;current view
(spec/def :navigation/view-id (spec/nilable keyword?))
;;modal view id
(spec/def :navigation/modal (spec/nilable keyword?))
;;stack of view's ids (keywords)
(spec/def :navigation/navigation-stack (spec/nilable seq?))
(spec/def :navigation/prev-tab-view-id (spec/nilable keyword?))
(spec/def :navigation/prev-view-id (spec/nilable keyword?))
;; navigation screen params
(spec/def :navigation.screen-params/network-details (allowed-keys :req [:networks/selected-network]))
(spec/def :navigation.screen-params/browser (allowed-keys :req [:browser/browser-id]))
(spec/def :navigation.screen-params.profile-qr-viewer/contact (spec/nilable map?))
(spec/def :navigation.screen-params.profile-qr-viewer/source (spec/nilable keyword?))
(spec/def :navigation.screen-params.profile-qr-viewer/value (spec/nilable string?))
(spec/def :navigation.screen-params/profile-qr-viewer (allowed-keys :opt-un [:navigation.screen-params.profile-qr-viewer/contact
                                                                             :navigation.screen-params.profile-qr-viewer/source
                                                                             :navigation.screen-params.profile-qr-viewer/value]))
(spec/def :navigation.screen-params.qr-scanner/current-qr-context (spec/nilable any?))
(spec/def :navigation.screen-params/qr-scanner (allowed-keys :opt-un [:navigation.screen-params.qr-scanner/current-qr-context]))
(spec/def :navigation.screen-params.group-contacts/show-search? (spec/nilable any?))
(spec/def :navigation.screen-params/group-contacts (allowed-keys :opt [:group/contact-group-id]
                                                                 :opt-un [:navigation.screen-params.group-contacts/show-search?]))
(spec/def :navigation.screen-params.edit-contact-group/group (spec/nilable any?))
(spec/def :navigation.screen-params.edit-contact-group/group-type (spec/nilable any?))
(spec/def :navigation.screen-params/edit-contact-group (allowed-keys :opt-un [:navigation.screen-params.edit-contact-group/group
                                                                              :navigation.screen-params.edit-contact-group/group-type]))
(spec/def :navigation.screen-params.dapp-description/dapp :new/open-dapp)
(spec/def :navigation.screen-params/dapp-description map?)

(spec/def :navigation.screen-params/usage-data vector?)

(spec/def :navigation/screen-params (spec/nilable (allowed-keys :opt-un [:navigation.screen-params/network-details
                                                                         :navigation.screen-params/browser
                                                                         :navigation.screen-params/profile-qr-viewer
                                                                         :navigation.screen-params/qr-scanner
                                                                         :navigation.screen-params/group-contacts
                                                                         :navigation.screen-params/edit-contact-group
                                                                         :navigation.screen-params/dapp-description
                                                                         :navigation.screen-params/usage-data])))

(spec/def :desktop/desktop (spec/nilable any?))

;;;;NETWORK

(spec/def ::network (spec/nilable string?))

;;;;NODE

(spec/def :node/after-start (spec/nilable vector?))
(spec/def :node/after-stop (spec/nilable vector?))

(spec/def ::message-envelopes (spec/nilable map?))

(spec/def ::db (allowed-keys
                 :opt
                 [:contacts/contacts
                  :contacts/new-identity
                  :contacts/new-public-key-error
                  :contacts/identity
                  :contacts/ui-props
                  :contacts/list-ui-props
                  :contacts/click-handler
                  :contacts/click-action
                  :contacts/click-params
                  :group/contact-groups
                  :group/contact-group-id
                  :group/group-type
                  :group/selected-contacts
                  :group/groups-order
                  :accounts/accounts
                  :accounts/create
                  :accounts/recover
                  :accounts/login
                  :account/account
                  :my-profile/profile
                  :my-profile/default-name
                  :my-profile/editing?
                  :my-profile/advanced?
                  :my-profile/seed
                  :group-chat-profile/profile
                  :group-chat-profile/editing?
                  :networks/selected-network
                  :networks/networks
                  :node/after-start
                  :node/after-stop
                  :inbox/wnodes
                  :inbox/password
                  :inbox/sym-key-id
                  :inbox/last-request
                  :browser/browsers
                  :browser/options
                  :new/open-dapp
                  :navigation/screen-params
                  :transport/message-envelopes
                  :transport/chats
                  :transport/discovery-filter
                  :desktop/desktop]
                 :opt-un
                 [::current-public-key
                  ::modal
                  ::was-modal?
                  ::rpc-url
                  ::web3
                  ::webview-bridge
                  ::status-module-initialized?
                  ::status-node-started?
                  ::keyboard-height
                  ::keyboard-max-height
                  ::tab-bar-visible?
                  ::network-status
                  ::mailserver-status
                  ::peers-count
                  ::sync-listening-started
                  ::sync-state
                  ::sync-data
                  ::network
                  :navigation/view-id
                  :navigation/navigation-stack
                  :navigation/prev-tab-view-id
                  :navigation/prev-view-id
                  :qr/qr-codes
                  :qr/qr-modal
                  :qr/current-qr-context
                  :chat/chats
                  :chat/current-chat-id
                  :chat/chat-id
                  :chat/new-chat
                  :chat/new-chat-name
                  :chat/chat-animations
                  :chat/chat-ui-props
                  :chat/chat-list-ui-props
                  :chat/layout-height
                  :chat/expandable-view-height-to-value
                  :chat/message-data
                  :chat/message-status
                  :chat/selected-participants
                  :chat/chat-loaded-callbacks
                  :chat/public-group-topic
                  :chat/public-group-topic-error
                  :chat/messages
                  :chat/not-loaded-message-ids
                  :chat/last-clock-value
                  :chat/loaded-chats
                  :chat/bot-db
                  :commands/access-scope->commands-responses
                  :discoveries/discoveries
                  :discoveries/discover-search-tags
                  :discoveries/discover-current-dapp
                  :discoveries/tags
                  :discoveries/current-tag
                  :discoveries/request-discoveries-timer
                  :wallet/wallet
                  :wallet/wallet.transactions
                  :wallet/wallet-selected-asset
                  :prices/prices
                  :prices/prices-loading?
                  :notifications/notifications]))
