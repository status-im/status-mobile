(ns status-im.ui.screens.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as spec]
            [status-im.constants :as constants]
            [status-im.utils.platform :as platform]
            status-im.ui.screens.accounts.db
            status-im.ui.screens.contacts.db
            status-im.ui.screens.qr-scanner.db
            status-im.ui.screens.group.db
            status-im.chat.specs
            status-im.chat.new-public-chat.db
            status-im.commands.specs
            status-im.ui.screens.profile.db
            status-im.ui.screens.discover.db
            status-im.ui.screens.network-settings.db
            status-im.ui.screens.offline-messaging-settings.db
            status-im.ui.screens.browser.db
            status-im.ui.screens.add-new.db))

;; initial state of app-db
(def app-db {:current-public-key         nil
             :status-module-initialized? (or platform/ios? js/goog.DEBUG)
             :keyboard-height            0
             :accounts/accounts          {}
             :navigation-stack           '()
             :contacts/contacts          {}
             :qr-codes                   {}
             :group/contact-groups       {}
             :group/selected-contacts    #{}
             :chats                      {}
             :current-chat-id            constants/console-chat-id
             :selected-participants      #{}
             :discoveries                {}
             :discover-search-tags       #{}
             :discover-current-dapp      {}
             :tags                       []
             :sync-state                 :done
             :wallet.transactions        constants/default-wallet-transactions
             :wallet-selected-asset      {}
             :prices                     {}
             :notifications              {}
             :network                    constants/default-network
             :networks/networks          constants/default-networks
             :inbox/wnode                constants/default-wnode
             :inbox/wnodes               constants/default-wnodes
             :inbox/topic                constants/inbox-topic
             :inbox/password             constants/inbox-password
             :my-profile/editing?        false})

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
;;:unknown - not used
(spec/def ::orientation (spec/nilable keyword?))
;;:online - presence of internet connection in the phone
(spec/def ::network-status (spec/nilable keyword?))

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
(spec/def :navigation.screen-params/contact (spec/nilable map?))
(spec/def :navigation.screen-params/qr-source (spec/nilable keyword?))
(spec/def :navigation.screen-params/qr-value (spec/nilable string?))
(spec/def :navigation.screen-params/qr-viewer (allowed-keys :opt-un [:navigation.screen-params/contact
                                                                     :navigation.screen-params/qr-source
                                                                     :navigation.screen-params/qr-value]))
(spec/def :navigation.screen-params/current-qr-context (spec/nilable any?))
(spec/def :navigation.screen-params/qr-scanner (allowed-keys :opt-un [:navigation.screen-params/current-qr-context]))
(spec/def :navigation.screen-params/show-search? (spec/nilable any?))
(spec/def :navigation.screen-params/group-contacts (allowed-keys :opt [:group/contact-group-id]
                                                                 :opt-un [:navigation.screen-params/show-search?]))
(spec/def :navigation.screen-params/group (spec/nilable any?))
(spec/def :navigation.screen-params/group-type (spec/nilable any?))
(spec/def :navigation.screen-params/edit-contact-group (allowed-keys :opt-un [:navigation.screen-params/group
                                                                              :navigation.screen-params/group-type]))
(spec/def :navigation/screen-params (spec/nilable (allowed-keys :opt-un [:navigation.screen-params/network-details
                                                                         :navigation.screen-params/browser
                                                                         :navigation.screen-params/qr-viewer
                                                                         :navigation.screen-params/qr-scanner
                                                                         :navigation.screen-params/group-contacts
                                                                         :navigation.screen-params/edit-contact-group])))

;;;;NETWORK

(spec/def ::network (spec/nilable string?))

;;;;NODE

(spec/def :node/after-start (spec/nilable vector?))
(spec/def :node/after-stop (spec/nilable vector?))

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
                  :accounts/account-creation?
                  :accounts/creating-account?
                  :accounts/current-account-id
                  :accounts/recover
                  :accounts/login
                  :my-profile/drawer
                  :my-profile/profile
                  :my-profile/default-name
                  :my-profile/editing?
                  :networks/selected-network
                  :networks/networks
                  :node/after-start
                  :node/after-stop
                  :inbox/wnode
                  :inbox/wnodes
                  :inbox/topic
                  :inbox/password
                  :browser/browsers
                  :browser/options
                  :new/open-dapp
                  :navigation/screen-params]
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
                  ::orientation
                  ::network-status
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
                  :chat/deleted-chats
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
