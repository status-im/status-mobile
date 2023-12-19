(ns status-im2.db
  (:require
    [legacy.status-im.fleet.core :as fleet]
    [legacy.status-im.wallet.db :as wallet.db]
    [react-native.core :as rn]
    [status-im2.contexts.shell.activity-center.events :as activity-center]))

;; initial state of app-db
(def app-db
  {:activity-center                    {:filter {:status (:filter-status activity-center/defaults)
                                                 :type   (:filter-type activity-center/defaults)}}
   :contacts/contacts                  {}
   :pairing/installations              {}
   :group/selected-contacts            #{}
   :chats                              {}
   :current-chat-id                    nil
   :group-chat/selected-participants   #{}
   :group-chat/deselected-members      #{}
   :sync-state                         :done
   :link-previews-whitelist            []
   :app-state                          "active"
   :wallet-legacy                      wallet.db/default-wallet
   :wallet-legacy/all-tokens           {}
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
   :toasts                             {:ordered '() :toasts {}}
   :tooltips                           {}
   :dimensions/window                  (rn/get-window)
   :registry                           {}
   :visibility-status-updates          {}
   :stickers/packs-pending             #{}
   :keycard                            {:nfc-enabled? false
                                        :pin          {:original     []
                                                       :confirmation []
                                                       :current      []
                                                       :puk          []
                                                       :enter-step   :original}}})
