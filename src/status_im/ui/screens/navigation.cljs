(ns status-im.ui.screens.navigation
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]))

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
   ;; TODO (jeluard) Unify all :navigate-to flavours. Maybe accept a map of parameters?

   (let [db (cond-> (assoc db :navigation-stack (list))
              (seq screen-params)
              (assoc-in [:navigation/screen-params view-id] screen-params))]
     {:db (push-view db view-id)})))

(defn replace-view [view-id {:keys [db]}]
  {:db (-> (update db :navigation-stack replace-top-element view-id)
           (assoc :view-id view-id))})

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

(defn navigate-to
  "Navigates to particular view"
  ([db go-to-view-id]
   (navigate-to db go-to-view-id nil))
  ([{:keys [view-id] :as db} go-to-view-id screen-params]
   (let [db (cond-> db
              (seq screen-params)
              (assoc-in [:navigation/screen-params go-to-view-id] screen-params))]
     (if (= view-id go-to-view-id)
       db
       (push-view db go-to-view-id)))))

(def unload-data-interceptor
  (re-frame/->interceptor
   :id unload-data-interceptor
   :before (fn unload-data-interceptor-before
             [context]
             (let [db (re-frame/get-coeffect context :db)]
               (re-frame/assoc-coeffect context :db (unload-data! db))))))

;; event handlers

(handlers/register-handler-db
 :navigate-to
 [unload-data-interceptor (re-frame/enrich preload-data!)]
 (fn [db [_ & params]]
   (apply navigate-to db params)))

(handlers/register-handler-db
 :navigate-to-modal
 [unload-data-interceptor (re-frame/enrich preload-data!)]
 (fn [db [_ modal-view]]
   (assoc db :modal modal-view)))

(handlers/register-handler-fx
 :navigation-replace
 [unload-data-interceptor (re-frame/enrich preload-data!)]
 (fn [cofx [_ view-id]]
   (replace-view view-id cofx)))

(defn navigate-back [{:keys [navigation-stack view-id modal] :as db}]
  (cond
    modal (assoc db :modal nil
                 :was-modal? true)
    (>= 1 (count navigation-stack)) db

    :else
    (let [[previous-view-id :as navigation-stack'] (pop navigation-stack)
          first-in-stack (first navigation-stack)]
      (if (= view-id first-in-stack)
        (-> db
            (assoc :view-id previous-view-id)
            (assoc :navigation-stack navigation-stack'))
        (assoc db :view-id first-in-stack)))))

(handlers/register-handler-db
 :navigate-back
 (re-frame/enrich -preload-data!)
 (fn [db _]
   (navigate-back db)))

(handlers/register-handler-fx
 :navigate-to-clean
 (fn [cofx [_ view-id params]]
   (navigate-to-clean view-id cofx params)))

(handlers/register-handler-fx
 :navigate-to-tab
 [unload-data-interceptor (re-frame/enrich preload-data!)]
 (fn [{:keys [db] :as cofx} [_ view-id]]
   (handlers-macro/merge-fx cofx
                            {:db (-> db
                                     (assoc :prev-tab-view-id (:view-id db))
                                     (assoc :prev-view-id (:view-id db)))}
                            (navigate-to-clean view-id))))
