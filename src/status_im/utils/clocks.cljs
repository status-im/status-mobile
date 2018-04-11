(ns status-im.utils.clocks
  (:require [status-im.utils.datetime :as utils.datetime]))

;; We use Lamport clocks to ensure correct ordering of events in chats. This is
;; necessary because we operate in a distributed system and there is no central
;; coordinator for what happened before what.
;;
;; We can't rely uniquely on timestamps as clocks might be different on each device.
;;
;; Received time cannot be used as it does not work with out-of-order messages.
;; If we used received time also each client could potentially have a different
;; ordering of messages, which would lead to some difficult misunderstanding
;; among participants.
;;
;; Lamport timestamps offer a consistent view across client, at the expenses of
;; understanding exactly at what time something has happened.
;; They satisfy the property: if a caused b then T(a) < T(b)
;;
;; In chat terms:
;;
;; Any message I send will always be displayed after any message I have seen,
;; including the messages I have sent.
;; This is a necessary condition to have a meaningful conversation with someone
;; and ought to be always true.
;;
;; We need to address another issue here:
;;
;; Even if I don't see all the messages, if I post a message I want that message
;; to be displayed last in a chat.
;;
;; That's were the basic algorithm of Lamport timestamp would fall short, as
;; it's only meant to order causally related events.
;;
;; If I join a public chat and I have not received any messages or I have missed
;; many messages because I was offline, when I post a new message it would be
;; displayed back in the history ( I would have to wait to receive a message
;; to bring my timestamp up-to-date).
;;
;; We cannot completely solve this as there's no way to know what the chat
;; current timestamp is without having to contact other peers ( which might all be offline)
;;
;; But what we can do, is to use our time to make a "bid", hoping that it will
;; beat the current chat-timestamp. So our Lamport timestamp format is:
;; {unix-timestamp-ms}{2-digits-post-id}
;;
;; We always need to make sure we take the max value between the last-clock-value
;; for the chat and the bid-timestamp.
;;
;; This will still satisfy Lamport requirement, namely: a -> b then T(a) < T(b)
;;
;; One way to think of this is as as Lamport timestamps where at every ms
;; an internal event is generated.
;;
;; In whisper v6 any message with a timestamp older than 20 seconds will be discarded.
;;
;; So worst case scenario is:
;; Your clock is 20 seconds behind, you join a public chat where everyone's clock
;; is 20 seconds ahead, you have not received 40s of inflight messages, you
;; publish. drama.
;; Your post will be displayed before any non-received inflight message.
;;
;; Once received the posts you will be able to communicate effectively, much rejoicing.
;; If there are no inflight messages then your post will be last.
;;
;; Posts sent when offline are more troublesome, as they would carry an old
;; timestamp, so the timestamp should be refreshed before retrying.
;;
;; Details:
;; https://en.wikipedia.org/wiki/Lamport_timestamps
;; http://amturing.acm.org/p558-lamport.pdf

(def one-month-in-ms (* 60 60 24 31 1000))
(def post-id-digits 100)

(defn- ->timestamp-bid []
  (* (utils.datetime/timestamp) post-id-digits))

; The timestamp has an upper limit of Number.MAX_SAFE_INTEGER
; A malicious client could send a crafted message with timestamp = Number.MAX_SAFE_INTEGER
; which effectively would DoS the chat, as any new message would get
; a timestamp of Number.MAX_SAFE_INTEGER (inc becomes a noop).
; We should never receive messages from untrusted peers with a timestamp greater
; then now + 20s.
; We cap the timestamp to time now + 1 month to give some room for trusted peers
(defn- safe-timestamp [t]
  (min t (* (+ one-month-in-ms (utils.datetime/timestamp)) post-id-digits)))

(defn send [local-clock]
  (inc (max local-clock (->timestamp-bid))))

(defn receive [message-clock local-clock]
  (-> (+ 1000 (max (or message-clock 0) (or local-clock 0)))
      safe-timestamp
      inc))
