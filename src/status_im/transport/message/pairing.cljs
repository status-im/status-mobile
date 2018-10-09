(ns status-im.transport.message.pairing
  (:require [cljs.spec.alpha :as spec]
            [status-im.transport.message.protocol :as protocol]
            [taoensso.timbre :as log]))

(defrecord SyncInstallation
           [contacts]
  protocol/StatusMessage
  (validate [this]
    (if (spec/valid? :message/sync-installation this)
      this
      (log/warn "failed sync installation validation" (spec/explain :message/sync-installation this)))))
