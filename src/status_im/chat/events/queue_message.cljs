(ns status-im.chat.events.queue-message
  (:require-macros [cljs.core.async.macros :as async-macros])
  (:require [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [status-im.utils.config :as config]
            [status-im.utils.pre-receiver :as pre-receiver]
            [status-im.utils.handlers :as handlers]))

;; We queue messaged before receiving them to allow lagging messages to catch up.
;; This ensures proper ordering from a user's POV the vast majority of the time.

;; XXX(oskarth): Hacky def, consider better encapsulation
(when config/queue-message-enabled?
  (def in-ch (pre-receiver/start!
              {:delay-ms 500
               :reorder? true
               :add-fn   #(re-frame/dispatch [:chat-received-message/add %])})))

;; NOTE(oskarth): in-ch is assumed to exist
(re-frame/reg-fx
 ::queue-message
 (fn [message]
   (async/put! in-ch message)))

(handlers/register-handler-fx
 :pre-received-message
 (fn [_ [_ message]]
   (if config/queue-message-enabled?
     {::queue-message message}
     {:dispatch [:chat-received-message/add message]})))
