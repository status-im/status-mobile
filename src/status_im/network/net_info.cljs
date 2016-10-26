(ns status-im.network.net-info
  (:require [status-im.utils.utils :as u]
            [taoensso.timbre :as log]))

(def net-info (u/get-react-property "NetInfo"))

(defn init [callback]
  (when net-info
    (.then (.fetch (.-isConnected net-info))
           (fn [is-connected?]
             (log/debug "Is connected?" is-connected?)
             (callback is-connected?)))))

(defn add-listener [listener]
  (when net-info
    (.addEventListener (.-isConnected net-info) "change" listener)))
