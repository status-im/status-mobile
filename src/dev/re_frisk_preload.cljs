(ns dev.re-frisk-preload
  (:require [re-frisk-remote.core :as re-frisk-remote]
            [status-im.config]))

(re-frisk-remote/enable {:host (str status-im.config/re-frisk-host ":" status-im.config/re-frisk-port)})
