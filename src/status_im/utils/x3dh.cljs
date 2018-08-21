(ns status-im.utils.x3dh
  (:require
   [re-frame.core :as re-frame]
   [status-im.utils.handlers :as handlers]))

(re-frame/reg-fx
 ::create
 (fn []
   (letfn [(callback [response]
             (if response
               (re-frame/dispatch [::created response])
               (re-frame/dispatch [::creation-failed])))]
     #_(status/create-x3dh-bundle callback))))

(handlers/register-handler-fx
 ::created
 (fn [cofx [_ bundle]]
   (println "BUNDLE CREATED" bundle)))

(handlers/register-handler-fx
 ::creation-failed
 (fn [cofx _]
   (println "BUNDLE FAILED")))

(defn create-fx [_]
  {::create true})
