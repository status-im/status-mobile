(ns status-backend.core
  (:require
   [status-backend.config :as config]
   [utils.network.core :as network]
   [clojure.string :as string]
   [oops.core :as oops]
   [taoensso.timbre :as log]))

(def ^:private default-fetch-params
  {:method  :POST
   :headers {"Accept"       "application/json"
             "Content-Type" "application/json"}})

(defn fetch
  ([endpoint]
   (fetch endpoint nil (fn [])))
  ([endpoint body-params]
   (fetch endpoint body-params (fn [])))
  ([endpoint body-params callback]
   (let [url    (str config/status-go-url endpoint)
         params (assoc default-fetch-params :body body-params)]
     (network/fetch url params callback))))

(defn- method-name->endpoint [s]
  (let [first-letter-capitalized (string/capitalize (first s))]
    (str first-letter-capitalized (subs s 1))))

(def fetch-js-obj
  "Performs a request to the status-backend (by using `status-backend.core/fetch`) using
  the method invoked as endpoint name. E.g
  This call: (.myMethod fetch-js-obj params)
  Is the same as: (fetch \"MyMethod\" params)
  "
  (js/Proxy. #js{}
             #js{:get (fn [_target native-module-method-name]
                        (let [endpoint (method-name->endpoint native-module-method-name)]
                          (partial fetch endpoint)))}))

(declare web-socket)

(defn init-web-socket [on-message]
  (defonce web-socket (js/WebSocket. config/signals-url))
  (oops/oset! web-socket "onopen" #(log/debug "[backend] Web Socket connected"))
  (oops/oset! web-socket "onmessage" #(-> % (oops/oget "data") (on-message)))
  (oops/oset! web-socket "onerror" #(log/error "[backend] Web Socket error" %))
  (oops/oset! web-socket "onclose" #(log/error "[backend] Web Socket closed" %)))
