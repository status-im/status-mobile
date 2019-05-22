(ns status-im.dev-server.core
  (:require [status-im.ui.components.react :as react]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            [re-frame.core :as re-frame]))

(defonce port 5561)
(defonce server-name (if platform/ios?
                       "Status iOS"
                       "Status Android"))

(defn respond! [request-id status-code data]
  (.respond (react/http-bridge)
            request-id
            status-code
            "application/json"
            (types/clj->json data)))

(defn start! []
  (.start (react/http-bridge)
          port
          server-name
          (fn [req]
            (try
              (let [{:keys [requestId url type postData]} (js->clj req :keywordize-keys true)
                    data (if (string? postData)
                           (-> (.parse js/JSON postData)
                               (js->clj :keywordize-keys true))
                           postData)]
                (re-frame/dispatch [:process-http-request requestId url type data]))
              (catch js/Error e
                (log/debug "Error: " e))))))

(defn stop! []
  (.stop (react/http-bridge)))
