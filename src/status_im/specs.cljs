(ns status-im.specs
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as spec]
            [status-im.accounts.specs]
            [status-im.navigation.specs]
            [status-im.contacts.db]
            [status-im.qr-scanner.specs]
            [status-im.group.db]
            [status-im.chat.specs]
            [status-im.chat.new-public-chat.db]
            [status-im.profile.specs]
            [status-im.transactions.specs]
            [status-im.discover.specs]))

;;;;GLOBAL

;;public key of current logged in account
(spec/def ::current-public-key (spec/nilable string?))
;;true when application running at first time
(spec/def ::first-run (spec/nilable boolean?))
(spec/def ::was-modal? (spec/nilable boolean?))
;;"http://localhost:8545"
(spec/def ::rpc-url (spec/nilable string?))
;;object? doesn't work
(spec/def ::web3 (spec/nilable any?))
;;object?
(spec/def ::webview-bridge (spec/nilable any?))
(spec/def ::status-module-initialized? (spec/nilable boolean?))
(spec/def ::status-node-started? (spec/nilable boolean?))
(spec/def ::toolbar-search (spec/nilable map?))
;;height of native keyboard if shown
(spec/def ::keyboard-height (spec/nilable number?))
(spec/def ::keyboard-max-height (spec/nilable number?))
;;:unknown - not used
(spec/def ::orientation (spec/nilable keyword?))
;;:online - presence of internet connection in the phone
(spec/def ::network-status (spec/nilable keyword?))

;;;;NODE

(spec/def ::sync-listening-started (spec/nilable boolean?))
(spec/def ::sync-state (spec/nilable keyword?))
(spec/def ::sync-data (spec/nilable map?))

;;;;NETWORK

;;network name :testnet
(spec/def ::network (spec/nilable keyword?))

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
                  :group/groups-order]
                 :opt-un
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
                  ::sync-data
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
