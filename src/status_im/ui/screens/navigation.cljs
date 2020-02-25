(ns status-im.ui.screens.navigation
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.ui.screens.routing.core :as navigation]
            [taoensso.timbre :as log]
            [status-im.utils.fx :as fx]))

(defmulti unload-data!
  (fn [db] (:view-id db)))

(defmethod unload-data! :default [db] db)

(fx/defn navigate-to-cofx
  [{:keys [db]} go-to-view-id screen-params]
  (let [db      (cond-> (assoc db :view-id go-to-view-id)

                  ;; TODO: Inspect the need of screen-params
                  (seq screen-params)
                  (assoc-in [:navigation/screen-params go-to-view-id]
                            screen-params))]
    {:db           db
     ::navigate-to [go-to-view-id screen-params]}))

(fx/defn navigate-replace-cofx
  [{:keys [db]} go-to-view-id screen-params]
  (let [db (cond-> (assoc db :view-id go-to-view-id)
             (seq screen-params)
             (assoc-in [:navigation/screen-params go-to-view-id]
                       screen-params))]
    {:db                db
     ::navigate-replace [go-to-view-id screen-params]}))

(fx/defn navigate-reset
  [_ config]
  {::navigate-reset config})

(def unload-data-interceptor
  (re-frame/->interceptor
   :id unload-data-interceptor
   :before (fn unload-data-interceptor-before
             [context]
             (let [db (re-frame/get-coeffect context :db)]
               (re-frame/assoc-coeffect context :db (unload-data! db))))))

(def navigation-interceptors
  [unload-data-interceptor])

(re-frame/reg-fx
 ::navigate-to
 (fn [[view-id params]]
   (log/debug :navigate-to view-id params)
   (navigation/navigate-to (name view-id) params)))

(re-frame/reg-fx
 ::navigate-replace
 (fn [[view-id params]]
   (log/debug :navigate-replace view-id params)
   (navigation/navigate-replace (name view-id) params)))

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

(handlers/register-handler-fx
 :navigate-to
 navigation-interceptors
 (fn [cofx [_ & [go-to-view-id screen-params]]]
   (navigate-to-cofx cofx go-to-view-id screen-params)))

(fx/defn navigate-back
  [_]
  {::navigate-back nil})

(handlers/register-handler-fx
 :navigate-back
 (fn [cofx _]
   (navigate-back cofx)))

(handlers/register-handler-fx
 :navigate-reset
 (fn [cofx [_ view-id]]
   (navigate-reset cofx {:index  0
                         :routes [{:name view-id}]})))

(handlers/register-handler-fx
 :navigate-to-clean
 (fn [cofx [_ view-id params]]
   (navigate-to-cofx cofx view-id params)))

(handlers/register-handler-fx
 :navigate-replace
 navigation-interceptors
 (fn [cofx [_ & [go-to-view-id screen-params]]]
   (navigate-replace-cofx cofx go-to-view-id screen-params)))
