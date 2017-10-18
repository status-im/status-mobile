(ns status-im.chat.specs
  (:require [cljs.spec.alpha :as s]))

(s/def :chat/chats (s/nilable map?))                                        ;; {id (string) chat (map)} active chats on chat's tab
(s/def :chat/current-chat-id (s/nilable string?))                           ;;current or last opened chat-id
(s/def :chat/chat-id (s/nilable string?))                                   ;;what is the difference ? ^
(s/def :chat/new-chat (s/nilable map?))                                     ;;used during adding new chat
(s/def :chat/new-chat-name (s/nilable string?))                             ;;we have name in the new-chat why do we need this field
(s/def :chat/chat-animations (s/nilable map?))                              ;;{id (string) props (map)}
(s/def :chat/chat-ui-props (s/nilable map?))                                ;;{id (string) props (map)}
(s/def :chat/chat-list-ui-props (s/nilable map?))
(s/def :chat/layout-height (s/nilable number?))                             ;;height of chat's view layout
(s/def :chat/expandable-view-height-to-value (s/nilable number?))
(s/def :chat/global-commands (s/nilable map?))                              ; {key (keyword) command (map)} atm used for browse command
(s/def :chat/loading-allowed (s/nilable boolean?))                          ;;allow to load more messages
(s/def :chat/handler-data (s/nilable map?))
(s/def :chat/message-data (s/nilable map?))
(s/def :chat/message-id->transaction-id (s/nilable map?))
(s/def :chat/message-status (s/nilable map?))
(s/def :chat/unviewed-messages (s/nilable map?))
(s/def :chat/selected-participants (s/nilable set?))
(s/def :chat/chat-loaded-callbacks (s/nilable map?))
(s/def :chat/commands-callbacks (s/nilable map?))
(s/def :chat/command-hash-valid? (s/nilable boolean?))
(s/def :chat/public-group-topic (s/nilable string?))
(s/def :chat/confirmation-code-sms-listener (s/nilable any?))               ; .addListener result object
(s/def :chat/messages (s/nilable seq?))
(s/def :chat/loaded-chats (s/nilable seq?))
(s/def :chat/bot-subscriptions (s/nilable map?))
(s/def :chat/new-request (s/nilable map?))
(s/def :chat/raw-unviewed-messages (s/nilable vector?))
(s/def :chat/bot-db (s/nilable map?))
(s/def :chat/geolocation (s/nilable map?))
