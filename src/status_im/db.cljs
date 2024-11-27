(ns status-im.db
  (:require
    [legacy.status-im.fleet.core :as fleet]
    [react-native.core :as rn]
    [status-im.contexts.shell.activity-center.events :as activity-center]
    [status-im.contexts.wallet.db :as wallet]))

;; initial state of app-db
(def app-db
  {:activity-center                    {:filter {:status (:filter-status activity-center/defaults)
                                                 :type   (:filter-type activity-center/defaults)}}
   :contacts/contacts                  {}
   :pairing/installations              {}
   :group/selected-contacts            #{}
   :chats                              {}
   :currencies                         {}
   :current-chat-id                    nil
   :group-chat/selected-participants   #{}
   :group-chat/deselected-members      #{}
   :sync-state                         :done
   :link-previews-whitelist            []
   :app-state                          "active"
   :wallet                             wallet/defaults
   :peer-stats/count                   0
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
   :settings/change-password           {}
   :keycard                            {}
   :theme                              :dark})
