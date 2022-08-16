(ns status-im.db
  (:require [status-im.utils.dimensions :as dimensions]
            [status-im.fleet.core :as fleet]
            [status-im.wallet.db :as wallet.db]))

;; initial state of app-db
(def app-db {:contacts/contacts                  {}
             :pairing/installations              {}
             :group/selected-contacts            #{}
             :chats                              {}
             :current-chat-id                    nil
             :selected-participants              #{}
             :sync-state                         :done
             :link-previews-whitelist            []
             :app-state                          "active"
             :wallet                             wallet.db/default-wallet
             :wallet/all-tokens                  {}
             :peers-count                        0
             :node-info                          {}
             :peers-summary                      []
             :transport/message-envelopes        {}
             :mailserver/mailservers             (fleet/default-mailservers {})
             :mailserver/topics                  {}
             :mailserver/pending-requests        0
             :chat/cooldowns                     0
             :chat/inputs                        {}
             :chat/cooldown-enabled?             false
             :chat/last-outgoing-message-sent-at 0
             :chat/spam-messages-frequency       0
             :chats-home-list                    #{}
             :home-items-show-number             20
             :tooltips                           {}
             :dimensions/window                  (dimensions/window)
             :registry                           {}
             :visibility-status-updates          {}
             :stickers/packs-pending             #{}
             :keycard                            {:nfc-enabled? false
                                                  :pin          {:original     []
                                                                 :confirmation []
                                                                 :current      []
                                                                 :puk          []
                                                                 :enter-step   :original}}})
