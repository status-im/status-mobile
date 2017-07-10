(ns status-im.specs
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as s]
            [status-im.accounts.specs]
            [status-im.navigation.specs]
            [status-im.contacts.specs]
            [status-im.qr-scanner.specs]
            [status-im.new-group.specs]
            [status-im.chat.specs]
            [status-im.profile.specs]
            [status-im.transactions.specs]
            [status-im.discover.specs]))

;GLOBAL
(s/def ::current-public-key string?)                        ;;public key of current logged in account
(s/def ::first-run boolean?)                                ;;true when application running at first time
(s/def ::was-modal? boolean?)
(s/def ::rpc-url string?)                                   ;;"http://localhost:8545"
(s/def ::web3 any?)                                         ;;object? doesn't work
(s/def ::webview-bridge any?)                               ;;object?
(s/def ::status-module-initialized? boolean?)
(s/def ::status-node-started? (s/nilable boolean?))
(s/def ::toolbar-search map?)
(s/def ::keyboard-height number?)                           ;;height of native keyboard if shown
(s/def ::keyboard-max-height number?)
(s/def ::orientation keyword?)                              ;;:unknown - not used
(s/def ::network-status (s/nilable keyword?))               ;;:online - presence of internet connection in the phone
;NODE
(s/def ::sync-listening-started boolean?)
(s/def ::sync-state keyword?)
;NETWORK
(s/def ::network keyword?)                                  ;;network name :testnet

(s/def ::db (allowed-keys :opt-un
                          [::current-public-key
                           ::first-run
                           ::modal
                           ::was-modal?
                           ::rpc-url
                           ::web3
                           ::webview-bridge
                           ::status-module-initialized?
                           ::status-node-started?
                           ::toolbar-search
                           ::keyboard-height
                           ::keyboard-max-height
                           ::orientation
                           ::network-status
                           ::sync-listening-started
                           ::sync-state
                           ::network
                           :accounts/accounts
                           :accounts/account-creation?
                           :accounts/creating-account?
                           :accounts/current-account-id
                           :accounts/recover
                           :accounts/login
                           :navigation/view-id
                           :navigation/navigation-stack
                           :navigation/prev-tab-view-id
                           :navigation/prev-view-id
                           :contacts/contacts
                           :contacts/new-contacts
                           :contacts/new-contact-identity
                           :contacts/new-contact-public-key-error
                           :contacts/contact-identity
                           :contacts/contacts-ui-props
                           :contacts/contact-list-ui-props
                           :contacts/contacts-click-handler
                           :contacts/contacts-click-action
                           :contacts/contacts-click-params
                           :qr/qr-codes
                           :qr/qr-modal
                           :qr/current-qr-context
                           :group/contact-groups
                           :group/contact-group-id
                           :group/group-type
                           :group/new-group
                           :group/new-groups
                           :group/contacts-group
                           :group/selected-contacts
                           :group/groups-order
                           :chats/chats
                           :chats/current-chat-id
                           :chats/chat-id
                           :chats/new-chat
                           :chats/new-chat-name
                           :chats/chat-animations
                           :chats/chat-ui-props
                           :chats/chat-list-ui-props
                           :chats/layout-height
                           :chats/expandable-view-height-to-value
                           :chats/global-commands
                           :chats/loading-allowed
                           :chats/message-data
                           :chats/message-id->transaction-id
                           :chats/message-status
                           :chats/unviewed-messages
                           :chats/selected-participants
                           :chats/chat-loaded-callbacks
                           :chats/commands-callbacks
                           :chats/command-hash-valid?
                           :chats/public-group-topic
                           :chats/confirmation-code-sms-listener
                           :chats/messages
                           :chats/loaded-chats
                           :chats/bot-subscriptions
                           :chats/new-request
                           :chats/raw-unviewed-messages
                           :profile/profile-edit
                           :transactions/transactions
                           :transactions/transactions-queue
                           :transactions/selected-transaction
                           :transactions/confirm-transactions
                           :transactions/confirmed-transactions-count
                           :transactions/transactions-list-ui-props
                           :transactions/transaction-details-ui-props
                           :transactions/wrong-password-counter
                           :transactions/wrong-password?
                           :discoveries/discoveries
                           :discoveries/discover-search-tags
                           :discoveries/tags
                           :discoveries/current-tag
                           :discoveries/request-discoveries-timer]))
