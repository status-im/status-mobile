(ns status-im.ui.screens.navigation
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.navigation :as navigation]
            [taoensso.timbre :as log]))

;; private helper fns

(defn- push-view [db view-id]
  (-> db
      (update :navigation-stack conj view-id)
      (assoc :view-id view-id)))

(defn- replace-top-element [stack view-id]
  (let [stack' (if (> 2 (count stack))
                 (list :home)
                 (pop stack))]
    (conj stack' view-id)))

;; public fns

(defn navigate-to-clean
  ([view-id cofx] (navigate-to-clean view-id cofx nil))
  ([view-id {:keys [db]} screen-params]
   (log/debug "current view-id " (:view-id db))
   (let [db (cond-> db
              (seq screen-params)
              (assoc-in [:navigation/screen-params view-id] screen-params))]
     {:db                 (push-view db view-id)
      ::navigate-to-clean view-id})))

(defn replace-view [view-id _]
  {::navigate-replace view-id})

(defn navigate-forget [view-id {:keys [db]}]
  {:db (assoc db :view-id view-id)})

(defmulti unload-data!
  (fn [db] (:view-id db)))

(defmethod unload-data! :default [db] db)

(defmulti preload-data!
  (fn [db [_ view-id]] (or view-id (:view-id db))))

(defmethod preload-data! :default [db _] db)

(defn- -preload-data! [{:keys [was-modal?] :as db} & args]
  (if was-modal?
    (dissoc db :was-modal?) ;;TODO check how it worked with this bug
    (apply preload-data! db args)))

(defn navigate-to-cofx [go-to-view-id screen-params {:keys [db]}]
  (let [view-id (:view-id db)
        db      (cond-> (assoc db :view-id go-to-view-id)
                  (seq screen-params)
                  (assoc-in [:navigation/screen-params go-to-view-id]
                            screen-params))]
    {:db           (if (= view-id go-to-view-id)
                     db
                     (push-view db go-to-view-id))
     ::navigate-to go-to-view-id}))

(defn navigate-reset
  [{:keys [index actions] :as config} {:keys [db]}]
  {:db              (assoc db :view-id
                           (:routeName (get actions index)))
   ::navigate-reset config})

(def unload-data-interceptor
  (re-frame/->interceptor
   :id unload-data-interceptor
   :before (fn unload-data-interceptor-before
             [context]
             (let [db (re-frame/get-coeffect context :db)]
               (re-frame/assoc-coeffect context :db (unload-data! db))))))

(def navigation-interceptors
  [unload-data-interceptor (re-frame/enrich preload-data!)])

;; effects

(re-frame/reg-fx
 ::navigate-to
 (fn [view-id]
   (log/debug :navigate-to view-id)
   (navigation/navigate-to (name view-id))))

(re-frame/reg-fx
 ::navigate-back
 (fn []
   (log/debug :navigate-back)
   (navigation/navigate-back)))

(re-frame/reg-fx
 ::navigate-replace
 (fn [view-id]
   (log/debug :navigate-replace view-id)
   (navigation/navigate-replace view-id)))

(re-frame/reg-fx
 ::navigate-reset
 (fn [config]
   (log/debug :navigate-reset config)
   (navigation/navigate-reset config)))

(re-frame/reg-fx
 ::navigate-to-clean
 (fn [view-id]
   (log/debug :navigate-to-clean view-id)
   (navigation/navigate-reset
    {:index   0
     :actions [{:routeName view-id}]})))

;; event handlers

(handlers/register-handler-fx
 :navigate-to
 navigation-interceptors
 (fn [cofx [_ & [go-to-view-id screen-params]]]
   (navigate-to-cofx go-to-view-id screen-params cofx)))

(handlers/register-handler-fx
 :navigate-to-modal
 navigation-interceptors
 (fn [{:keys [db]} [_ modal-view]]
   {:db (assoc db :modal modal-view)}))

(handlers/register-handler-fx
 :navigation-replace
 navigation-interceptors
 (fn [cofx [_ view-id]]
   (replace-view view-id cofx)))

(defn navigate-back
  [{{:keys [navigation-stack view-id] :as db} :db}]
  (assoc
   {::navigate-back nil}
   :db (let [[previous-view-id :as navigation-stack'] (pop navigation-stack)
             first-in-stack (first navigation-stack)]
         (if (= view-id first-in-stack)
           (-> db
               (assoc :view-id previous-view-id)
               (assoc :navigation-stack navigation-stack'))
           (assoc db :view-id first-in-stack)))))

(handlers/register-handler-fx
 :navigate-back
 (re-frame/enrich -preload-data!)
 (fn [cofx _]
   (navigate-back cofx)))

(handlers/register-handler-fx
 :navigate-to-clean
 (fn [cofx [_ view-id params]]
   (navigate-to-clean view-id cofx params)))

(handlers/register-handler-fx
 :navigate-to-tab
 navigation-interceptors
 (fn [{:keys [db] :as cofx} [_ view-id]]
   (handlers-macro/merge-fx cofx
                            {:db (-> db
                                     (assoc :prev-tab-view-id (:view-id db))
                                     (assoc :prev-view-id (:view-id db)))}
                            (navigate-to-cofx view-id {}))))
