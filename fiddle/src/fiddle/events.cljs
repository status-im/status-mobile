(ns fiddle.events
  (:require [re-frame.core :as re-frame]
            [cljs.reader :as reader]))

(re-frame/reg-event-fx
 :initialize-db
 (fn [_ _]
   {:db {:view-id :colors}}))

(re-frame/reg-fx
 :load-icons-fx
 (fn []
   (let [cl (js/XMLHttpRequest.)]
     (.open cl "GET" "./icons.edn")
     (set! (.-onreadystatechange cl) #(when (= (.-status cl) 200)
                                        (re-frame/dispatch [:set :icons (reader/read-string (.-responseText cl))])))
     (.send cl))))


(re-frame/reg-event-fx
 :load-icons
 (fn [_ _]
   {:load-icons-fx nil}))

(re-frame/reg-event-fx
 :set
 (fn [{:keys [db]} [_ k v]]
   {:db (assoc db k v)}))

(re-frame/reg-event-fx
 :set-in
 (fn [{:keys [db]} [_ path v]]
   {:db (assoc-in db path v)}))