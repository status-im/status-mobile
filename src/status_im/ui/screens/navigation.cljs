(ns status-im.ui.screens.navigation
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :refer [register-handler-db]]
            [status-im.constants :refer [console-chat-id]]))

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

(defn- replace-view [db view-id]
  (-> db
      (update :navigation-stack replace-top-element view-id)
      (assoc :view-id view-id)))

(defn- can-navigate-back? [db]
  (not (get db :accounts/creating-account?)))

(defn- navigate-to-clean [db view-id]
  (-> db
      (assoc :navigation-stack (list))
      (push-view view-id)))

;; public fns

(defmulti preload-data!
  (fn [db [_ view-id]] (or view-id (:view-id db))))

(defmethod preload-data! :default [db _] db)

(defn- -preload-data! [{:keys [was-modal?] :as db} & args]
  (if was-modal?
    (dissoc db :was-modal?) ;;TODO check how it worked with this bug
    (apply preload-data! db args)))

(defn navigate-to
  "Navigates to particular view"
  [{:keys [view-id] :as db} go-to-view-id]
  (if (= view-id go-to-view-id)
    db
    (push-view db go-to-view-id)))

;; event handlers

(register-handler-db
  :navigate-forget
  (re-frame/enrich preload-data!)
  (fn [db [_ new-view-id]]
    (assoc db :view-id new-view-id)))

(register-handler-db
  :navigate-to
  (re-frame/enrich preload-data!)
  (fn [db [_ new-view-id]]
    (navigate-to db new-view-id)))

(register-handler-db
  :navigate-to-modal
  (re-frame/enrich preload-data!)
  (fn [db [_ modal-view]]
    (assoc db :modal modal-view)))

(register-handler-db
  :navigation-replace
  (re-frame/enrich preload-data!)
  (fn [db [_ view-id]]
    (replace-view db view-id)))

(register-handler-db
  :navigate-back
  (re-frame/enrich -preload-data!)
  (fn [{:keys [navigation-stack view-id modal] :as db} _]
    (cond
      modal (assoc db :modal nil
                   :was-modal? true)
      (>= 1 (count navigation-stack)) db

      :else
      (if (can-navigate-back? db)
        (let [[previous-view-id :as navigation-stack'] (pop navigation-stack)
              first-in-stack (first navigation-stack)]
          (if (= view-id first-in-stack)
            (-> db
                (assoc :view-id previous-view-id)
                (assoc :navigation-stack navigation-stack'))
            (assoc db :view-id first-in-stack)))
        db))))

(register-handler-db
  :navigate-to-clean
  (fn [db [_ view-id]]
    (navigate-to-clean db view-id)))

(register-handler-db
  :navigate-to-tab
  (re-frame/enrich preload-data!)
  (fn [db [_ view-id]]
    (-> db
        (assoc :prev-tab-view-id (:view-id db))
        (assoc :prev-view-id (:view-id db))
        (navigate-to-clean view-id))))
