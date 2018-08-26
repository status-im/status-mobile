(ns status-im.signals.core
  (:require [status-im.init.core :as init]
            [status-im.transport.handlers :as transport.handlers]
            [status-im.transport.inbox :as inbox]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(defn summary [peers-summary {:keys [db] :as cofx}]
  (let [previous-summary (:peers-summary db)
        peers-count      (count peers-summary)]
    (handlers-macro/merge-fx cofx
                             {:db (assoc db
                                         :peers-summary peers-summary
                                         :peers-count peers-count)}
                             (transport.handlers/resend-contact-messages previous-summary)
                             (inbox/peers-summary-change-fx previous-summary))))

(defn process [event-str cofx]
  (let [{:keys [type event]} (types/json->clj event-str)]
    (case type
      "node.started"       (init/status-node-started cofx)
      "node.stopped"       (init/status-node-stopped cofx)
      "module.initialized" (init/status-module-initialized cofx)
      "envelope.sent"      (transport.handlers/update-envelope-status (:hash event) :sent cofx)
      "envelope.expired"   (transport.handlers/update-envelope-status (:hash event) :sent cofx)
      "discovery.summary"  (summary event cofx)
      (log/debug "Event " type " not handled"))))
