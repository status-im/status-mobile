(ns status-im.utils.universal-links.events
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.config :as config]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.universal-links.core :as universal-links]))

(handlers/register-handler-fx
 :handle-universal-link
 (fn [cofx [_ url]]
   (log/debug "universal links: event received for " url)
   (universal-links/handle-url url cofx)))
