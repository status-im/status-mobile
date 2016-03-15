(ns messenger.comm.pubsub
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [cljs.core.async :as async :refer [chan pub sub]]
            [messenger.state :refer [state
                                     pub-sub-publisher
                                     app-state
                                     pub-sub-path]]
            [messenger.comm.services :refer [services-handler]]
            [messenger.utils.event :refer [handle-channel-events]]))

(defn service-id [message]
  (first message))

(defn payload [message]
  (rest message))

(defn subscribe-handler [publication topic handler]
  (let [chn (chan)]
    (sub publication topic chn)
    (handle-channel-events chn (fn [_topic message]
                                 (handler app-state
                                          (service-id message)
                                          (payload message))))))

(defn setup-publication! [app-state]
  (let [publisher   (pub-sub-publisher @app-state)
        publication (pub publisher first)]
    (swap! app-state assoc-in pub-sub-path publication)
    publication))

(defn setup-pub-sub []
  (-> (setup-publication! app-state)
      (subscribe-handler :service services-handler)))