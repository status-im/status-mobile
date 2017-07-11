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
(s/def ::current-public-key (s/nilable string?))                        ;;public key of current logged in account
(s/def ::first-run (s/nilable boolean?))                                ;;true when application running at first time
(s/def ::was-modal? (s/nilable boolean?))
(s/def ::rpc-url (s/nilable string?))                                   ;;"http://localhost:8545"
(s/def ::web3 (s/nilable any?))                                         ;;object? doesn't work
(s/def ::webview-bridge (s/nilable any?))                               ;;object?
(s/def ::status-module-initialized? (s/nilable boolean?))
(s/def ::status-node-started? (s/nilable boolean?))
(s/def ::toolbar-search (s/nilable map?))
(s/def ::keyboard-height (s/nilable number?))                           ;;height of native keyboard if shown
(s/def ::keyboard-max-height (s/nilable number?))
(s/def ::orientation (s/nilable keyword?))                              ;;:unknown - not used
(s/def ::network-status (s/nilable keyword?))               ;;:online - presence of internet connection in the phone
;NODE
(s/def ::sync-listening-started (s/nilable boolean?))
(s/def ::sync-state (s/nilable keyword?))
;NETWORK
(s/def ::network (s/nilable keyword?))                                  ;;network name :testnet

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
                           :chat/global-commands
                           :chat/loading-allowed
                           :chat/message-data
                           :chat/message-id->transaction-id
                           :chat/message-status
                           :chat/unviewed-messages
                           :chat/selected-participants
                           :chat/chat-loaded-callbacks
                           :chat/commands-callbacks
                           :chat/command-hash-valid?
                           :chat/public-group-topic
                           :chat/confirmation-code-sms-listener
                           :chat/messages
                           :chat/loaded-chats
                           :chat/bot-subscriptions
                           :chat/new-request
                           :chat/raw-unviewed-messages
                           :chat/bot-db
                           :chat/geolocation
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
                           :discoveries/request-discoveries-timer
                           :discoveries/new-discover]))
