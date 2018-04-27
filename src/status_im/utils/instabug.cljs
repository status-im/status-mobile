(ns status-im.utils.instabug
  (:require [taoensso.timbre :as log]
            [status-im.utils.config :as config]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

(def instabug rn-dependencies/instabug)

;; `event` is an event name, e.g. "Tap"
;; `properties` is a map of event details or nil, e.g. {:target :send-current-message}
;; (see mixpanel_events.edn for list of trackable events)

(def survey-triggers
  [{:event :send-current-message :count 4  :token "UqtvIKgVDUTo4l_sDS-fwA"}
   {:event :send-current-message :count 29 :token "Hr9Dk3krPK7PPxuDbHAmXg"}])

;; 2018-05-07 12:00:00
(def survey-enabled-timestamp 1525694400000)

(defn maybe-show-survey [db]
  (when config/instabug-surveys-enabled?
    (let [sent-messages (->> db
                             :chats
                             (filter (fn [[chat-name _]] (not= "console" chat-name)))
                             (map second)
                             (mapcat (comp vals :messages))
                             (filter :outgoing))
          sent-messages-count (count sent-messages)
          sent-messages-after-ts-count (->> sent-messages
                                            (filter #(> (:ts %) survey-enabled-timestamp))
                                            count)
          {:keys [token]} (first (filter (fn [{:keys [count]}]
                                           (or
                                             (= count sent-messages-count)
                                             (= count sent-messages-after-ts-count)))
                                         survey-triggers))]
      (when token
        (.showSurveyWithToken instabug token)))))

(defn track [event properties]
  ;; NOTE(dmitryn) disabled until Instabug fix User Events
  #_(.logUserEventWithNameAndParams instabug event properties))

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
