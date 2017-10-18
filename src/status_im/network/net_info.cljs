(ns status-im.network.net-info
  (:require [taoensso.timbre :as log]
            [status-im.ui.components.react :as react-components]))

(defn init [callback]
  (when react-components/net-info
    (.then (.fetch (.-isConnected react-components/net-info))
           (fn [is-connected?]
             (log/debug "Is connected?" is-connected?)
             (callback is-connected?)))))

(defn add-listener [listener]
  (when react-components/net-info
    (.addEventListener (.-isConnected react-components/net-info) "change" listener)))
