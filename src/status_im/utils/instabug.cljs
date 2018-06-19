(ns status-im.utils.instabug
  (:require [taoensso.timbre :as log]
            [status-im.utils.config :as config]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

(def instabug rn-dependencies/instabug)

(defn submit-bug []
  (.invokeWithInvocationMode
   instabug
   (.. instabug
       -invocationMode
       -newBug)))

(defn request-feature []
  (.showFeatureRequests
   instabug))

(defn- prepare-event-name [event {:keys [target]}]
  (str event " " target))

;; `event` is an event name, e.g. "Tap"
;; `properties` is a map of event details or nil, e.g. {:target :send-current-message}
;; (see status-im.utils.mixpanel-events for list of trackable events)
(defn track [event properties]
  (when (= event "Tap")
    (let [event-name (prepare-event-name event properties)]
      (try
        (.logUserEventWithName instabug event-name)
        (catch :default _ nil)))))

(defn log [str]
  (if js/goog.DEBUG
    (log/debug str)
    (.IBGLog rn-dependencies/instabug str)))

(defn instabug-appender []
  {:enabled?   true
   :async?     false
   :min-level  nil
   :rate-limit nil
   :output-fn  :inherit

   :fn         (fn [data]
                 (let [{:keys [level ?ns-str ?err output_]} data]
                   (log (force output_))))})

(when-not js/goog.DEBUG
  (log/merge-config! {:appenders {:instabug (instabug-appender)}}))

(defn init []
  (.startWithToken rn-dependencies/instabug
                   config/instabug-token
                   (.. rn-dependencies/instabug -invocationEvent -shake))
  (.setIntroMessageEnabled rn-dependencies/instabug false))
