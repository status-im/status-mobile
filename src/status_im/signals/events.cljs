(ns status-im.signals.events
  (:require [status-im.signals.core :as signals]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.instabug :as instabug]
            [taoensso.timbre :as log]))

(handlers/register-handler-fx
 :signal-event
 (fn [cofx [_ event-str]]
   (log/debug :event-str event-str)
   (instabug/log (str "Signal event: " event-str))
   (signals/process event-str cofx)))
