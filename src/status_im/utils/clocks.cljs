(ns status-im.utils.clocks)

;; We use Lamport clocks to ensure correct ordering of events in chats. This is
;; necessary because we operate in a distributed system and there is no central
;; coordinator for what happened before what.
;;
;; For example, the last received message in a group chat will appear last,
;; regardless if that person has seen all the previous group chat messages. The
;; principal invariant to maintain is that clock-values should be monotonically
;; increasing.
;;
;; All clock updates happens as part of sending or receiving a message. Here's
;; the basic algorithm:
;;
;; Sending messages:
;; time = time+1;
;; time_stamp = time;
;; send(message, time_stamp);
;;
;; Receiving messages:
;; (message, time_stamp) = receive();
;; time = max(time_stamp, time)+1;
;;
;; Details:
;; https://en.wikipedia.org/wiki/Lamport_timestamps
;; http://amturing.acm.org/p558-lamport.pdf

(defn send [local-clock]
  (inc (or local-clock 0)))

(defn receive [message-clock local-clock]
  (inc (max (or message-clock 0) (or local-clock 0))))
