(ns status-im.dev-server.events
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.dev-server.core :as dev-server.core]
            [status-im.models.dev-server :as models.dev-server]
            [status-im.utils.handlers :as handlers]))

;; FX

(re-frame/reg-fx
 :dev-server/start
 (fn []
   (dev-server.core/start!)))

(re-frame/reg-fx
 :dev-server/stop
 (fn []
   (dev-server.core/stop!)))

(re-frame/reg-fx
 :dev-server/respond
 (fn [[request-id status-code data]]
   (dev-server.core/respond! request-id status-code data)))

;; Handlers

(handlers/register-handler-fx
 :process-http-request
 [(re-frame/inject-cofx :random-id-generator)]
 (fn [cofx [_ request-id url type data]]
   (try
     (models.dev-server/process-request! {:cofx       cofx
                                          :request-id request-id
                                          :url        (rest (string/split url "/"))
                                          :type       (keyword type)
                                          :data       data})
     (catch js/Error e
       {:dev-server/respond [request-id 400 {:message (str "Unsupported operation: " e)}]}))))
