(ns status-im.chat.specs
  (:require [cljs.spec.alpha :as s]))

(s/def :chat/chats map?)                                        ;; {id (string) chat (map)} active chats on chat's tab
(s/def :chat/current-chat-id string?)                           ;;current or last opened chat-id
(s/def :chat/chat-id string?)                                   ;;what is the difference ? ^
(s/def :chat/new-chat map?)                                     ;;used during adding new chat
(s/def :chat/new-chat-name string?)                             ;;we have name in the new-chat why do we need this field
(s/def :chat/chat-animations map?)                              ;;{id (string) props (map)}
(s/def :chat/chat-ui-props map?)                                ;;{id (string) props (map)}
(s/def :chat/chat-list-ui-props map?)
(s/def :chat/layout-height number?)                             ;;height of chat's view layout
(s/def :chat/expandable-view-height-to-value number?)
(s/def :chat/global-commands map?)                              ; {key (keyword) command (map)} atm used for browse command
(s/def :chat/loading-allowed boolean?)                          ;;allow to load more messages
(s/def :chat/message-data map?)
(s/def :chat/message-id->transaction-id map?)
(s/def :chat/message-status map?)
(s/def :chat/unviewed-messages (s/nilable map?))
(s/def :chat/selected-participants set?)
(s/def :chat/chat-loaded-callbacks map?)
(s/def :chat/commands-callbacks map?)
(s/def :chat/command-hash-valid? boolean?)
(s/def :chat/public-group-topic string?)
(s/def :chat/confirmation-code-sms-listener any?)               ; .addListener result object
(s/def :chat/messages seq?)
(s/def :chat/loaded-chats seq?)
(s/def :chat/bot-subscriptions map?)
(s/def :chat/new-request map?)
(s/def :chat/raw-unviewed-messages vector?)
(s/def :chat/bot-db map?)
(s/def :chat/geolocation map?)