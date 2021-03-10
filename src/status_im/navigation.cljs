(ns status-im.navigation
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.routing.core :as navigation]
            [taoensso.timbre :as log]
            [status-im.utils.fx :as fx]
            [status-im.anon-metrics.interceptors :as anon-metrics]))

(re-frame/reg-fx
 ::navigate-to
 (fn [[view-id params]]
   (log/debug :navigate-to view-id params)
   (navigation/navigate-to (name view-id) params)))

(re-frame/reg-fx
 ::navigate-back
 (fn []
   (log/debug :navigate-back)
   (navigation/navigate-back)))

(re-frame/reg-fx
 ::navigate-reset
 (fn [config]
   (log/debug :navigate-reset config)
   (navigation/navigate-reset config)))

(re-frame/reg-fx
 ::navigate-replace
 (fn [[view-id params]]
   (log/debug :navigate-replace view-id params)
   (navigation/navigate-replace (name view-id) params)))

(defn- all-screens-params [db view screen-params]
  (cond-> db
    (and (seq screen-params) (:screen screen-params) (:params screen-params))
    (all-screens-params (:screen screen-params) (:params screen-params))

    (seq screen-params)
    (assoc-in [:navigation/screen-params view] screen-params)))

(fx/defn navigate-to-cofx
  [{:keys [db]} go-to-view-id screen-params]
  {:db
   (-> (assoc db :view-id go-to-view-id)
       (all-screens-params go-to-view-id screen-params))
   ::navigate-to [go-to-view-id screen-params]})

(fx/defn navigate-to
  {:events       [:navigate-to]
   :interceptors [anon-metrics/catch-events]}
  [cofx go-to-view-id screen-params]
  (navigate-to-cofx cofx go-to-view-id screen-params))

(fx/defn navigate-back
  {:events [:navigate-back]}
  [_]
  {::navigate-back nil})

(fx/defn navigate-reset
  {:events [:navigate-reset]}
  [_ config]
  {::navigate-reset config})

(fx/defn navigate-replace
  {:events [:navigate-replace]
   :interceptors [anon-metrics/catch-events]}
  [{:keys [db]} go-to-view-id screen-params]
  (let [db (cond-> (assoc db :view-id go-to-view-id)
             (seq screen-params)
             (assoc-in [:navigation/screen-params go-to-view-id] screen-params))]
    {:db                db
     ::navigate-replace [go-to-view-id screen-params]}))
