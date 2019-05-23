(ns status-im.network.net-info
  (:require [taoensso.timbre :as log]
            [status-im.ui.components.react :as react-components]
            [status-im.utils.platform :as platform]))

(defn is-connected? [callback]
  (when (react-components/net-info)
    (.then (.fetch (.-isConnected (react-components/net-info)))
           (fn [is-connected?]
             (log/debug "Is connected?" is-connected?)
             (callback is-connected?)))))

(defn- wrap-net-info [callback]
  (fn [info-js]
    (let [info       (js->clj info-js :keywordize-keys true)
          on-success #(callback {:type (:type info) :expensive? %})]
      (if platform/ios?
        (on-success false)
        (.. ^js (react-components/net-info)
            isConnectionExpensive
            (then on-success)
            (catch (fn [error] (log/warn "isConnectionExpensive: " error))))))))

(defn net-info [callback]
  (when (react-components/net-info)
    (.then (.getConnectionInfo ^js (react-components/net-info))
           (wrap-net-info callback))))

(defn add-connection-listener [listener]
  (when (react-components/net-info)
    (.addEventListener (.-isConnected (react-components/net-info)) "connectionChange" listener)))

(defn add-net-info-listener [listener]
  (when (react-components/net-info)
    (.addEventListener (react-components/net-info) "connectionChange"
                       (wrap-net-info listener))))
