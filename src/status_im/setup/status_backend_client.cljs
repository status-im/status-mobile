(ns status-im.setup.status-backend-client
  (:require ["react-native" :as react-native]
            [status-im.config :as config]))

(def default-config
  {:server-enabled?    (config/enabled? config/STATUS_BACKEND_SERVER_ENABLED)
   :status-go-endpoint (str "http://" config/STATUS_BACKEND_SERVER_HOST "/statusgo/")
   :signal-endpoint    (str "ws://" config/STATUS_BACKEND_SERVER_HOST "/signals")
   :root-data-dir      config/STATUS_BACKEND_SERVER_ROOT_DATA_DIR})

(defn set-config!
  [{:keys [server-enabled? status-go-endpoint signal-endpoint root-data-dir]}]
  (when-let [client (.-StatusBackendClient (.-NativeModules react-native))]
    (.configStatusBackendServer client
                                server-enabled?
                                status-go-endpoint
                                signal-endpoint
                                root-data-dir)))

(defn init
  []
  (set-config! default-config))
