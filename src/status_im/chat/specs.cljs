(ns status-im.chat.specs
  (:require [cljs.spec.alpha :as s]))

(s/def :chat/chats (s/nilable map?))                              ; {id (string) chat (map)} active chats on chat's tab
(s/def :chat/current-chat-id (s/nilable string?))                 ; current or last opened chat-id
(s/def :chat/chat-id (s/nilable string?))                         ; what is the difference ? ^
(s/def :chat/new-chat-name (s/nilable string?))                   ; we have name in the new-chat why do we need this field
(s/def :chat/chat-animations (s/nilable map?))                    ; {id (string) props (map)}
(s/def :chat/chat-ui-props (s/nilable map?))                      ; {id (string) props (map)}
(s/def :chat/chat-list-ui-props (s/nilable map?))
(s/def :chat/layout-height (s/nilable number?))                   ; height of chat's view layout
(s/def :chat/expandable-view-height-to-value (s/nilable number?))
(s/def :chat/selected-participants (s/nilable set?))
(s/def :chat/chat-loaded-callbacks (s/nilable map?))
(s/def :chat/public-group-topic (s/nilable string?))
(s/def :chat/public-group-topic-error (s/nilable string?))
(s/def :chat/messages (s/nilable map?))                           ; messages indexed by message-id
(s/def :chat/message-groups (s/nilable map?))                     ; grouped/sorted messages
(s/def :chat/message-statuses (s/nilable map?))                   ; message/user statuses indexed by two level index
(s/def :chat/not-loaded-message-ids (s/nilable set?))             ; set of message-ids not yet fully loaded from persisted state
(s/def :chat/last-clock-value (s/nilable number?))                ; last logical clock value of messages in chat
(s/def :chat/loaded-chats (s/nilable seq?))
(s/def :chat/bot-db (s/nilable map?))
(s/def :chat/cooldowns (s/nilable number?))                       ; number of cooldowns given for spamming send button
(s/def :chat/cooldown-enabled? (s/nilable boolean?))
(s/def :chat/last-outgoing-message-sent-at (s/nilable number?))
(s/def :chat/spam-messages-frequency (s/nilable number?))         ; number of consecutive spam messages sent
