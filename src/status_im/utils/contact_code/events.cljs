(ns status-im.utils.contact-code.events
  (:require
   [re-frame.core :as re-frame]
   status-im.utils.contact-code.subs
   [status-im.utils.contact-code.model :as contact-code]
   [status-im.utils.handlers :as handlers]))

(re-frame/reg-fx
 ::create
 (fn []
   (letfn [(callback [response]
             (if response
               (re-frame/dispatch [::created response])
               (re-frame/dispatch [::creation-failed])))]
     (contact-code/create! callback))))

(handlers/register-handler-fx
 ::created
 (fn [cofx [_ bundle]]
   (contact-code/add bundle cofx)))

(defn create-fx [_]
  {::create true})
