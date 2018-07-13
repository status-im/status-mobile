(ns status-im.ui.screens.signal-events
  (:require [status-im.utils.handlers :as handlers]
            [taoensso.timbre :as log]
            [status-im.utils.instabug :as instabug]
            [status-im.utils.types :as types]
            [status-im.thread :as status-im.thread]))

;; Because we send command to jail in params and command `:ref` is a lookup vector with
;; keyword in it (for example `["transactor" :command 51 "send"]`), we lose that keyword
;; information in the process of converting to/from JSON, and we need to restore it
(defn- restore-command-ref-keyword
  [orig-params]
  (if [(get-in orig-params [:command :command :ref])]
    (update-in orig-params [:command :command :ref 1] keyword)
    orig-params))

(defn handle-jail-signal [{:keys [chat_id data]}]
  (let [{:keys [event data]} (types/json->clj data)]
    (case event
      "local-storage"    [:set-local-storage {:chat-id chat_id
                                              :data    data}]
      "show-suggestions" [:show-suggestions-from-jail {:chat-id chat_id
                                                       :markup  data}]
      "send-message"     [:chat-send-message/from-jail {:chat-id chat_id
                                                        :message data}]
      "handler-result"   (let [orig-params (:origParams data)]
                           ;; TODO(janherich): figure out and fix chat_id from event
                           [:command-handler! (:chat-id orig-params)
                            (restore-command-ref-keyword orig-params)
                            {:result {:returned (dissoc data :origParams)}}])
      (log/debug "Unknown jail signal " event))))

(handlers/register-handler-fx
 :signal-event
 (fn [_ [_ event-str]]
   (log/debug :event-str event-str)
   (instabug/log (str "Signal event: " event-str))
   (let [{:keys [type event]} (types/json->clj event-str)
         to-dispatch (case type
                       "sign-request.queued" [:sign-request-queued event]
                       "sign-request.failed" [:sign-request-failed event]
                       "node.started" [:status-node-started]
                       "node.stopped" [:status-node-stopped]
                       "module.initialized" [:status-module-initialized]
                       "jail.signal" (handle-jail-signal event)
                       "envelope.sent" [:signals/envelope-status (:hash event) :sent]
                       "envelope.expired" [:signals/envelope-status (:hash event) :not-sent]
                       "discovery.summary" [:discovery/summary event]
                       (log/debug "Event " type " not handled"))]
     (when to-dispatch
       (status-im.thread/dispatch to-dispatch)))))
