(ns status-im.navigation.handlers
  (:require [re-frame.core :refer [register-handler dispatch debug enrich
                                   after]]))

(defn push-view [db view-id]
  (-> db
      (update :navigation-stack conj view-id)
      (assoc :view-id view-id)))

(defn replace-top-element [stack view-id]
  (let [stack' (if (pos? (count stack))
                 (pop stack)
                 stack)]
    (conj stack' view-id)))

(defn replace-view [db view-id]
  (-> db
      (update :navigation-stack replace-top-element view-id)
      (assoc :view-id view-id)))

(defmulti preload-data!
          (fn [db [_ view-id]] (or view-id (:view-id db))))

(defmethod preload-data! :default [db _] db)

(register-handler :navigate-to
  (enrich preload-data!)
  (fn [db [_ view-id]]
    (push-view db view-id)))

(register-handler :navigation-replace
  (enrich preload-data!)
  (fn [db [_ view-id]]
    (replace-view db view-id)))

(register-handler :navigate-back
  (enrich preload-data!)
  (fn [{:keys [navigation-stack] :as db} _]
    (if (>= 1 (count navigation-stack))
      db
      (let [[view-id :as navigation-stack'] (pop navigation-stack)]
        (-> db
            (assoc :view-id view-id)
            (assoc :navigation-stack navigation-stack'))))))

(register-handler :show-group-new
  (debug
    (fn [db _]
      (-> db
          (push-view :new-group)
          (assoc :new-group #{})))))

(register-handler :show-contacts
  (fn [db _]
    (push-view db :contact-list)))

(defn show-profile
  [db [_ identity]]
  (-> db
      (assoc :contact-identity identity)
      (push-view :profile)))

(register-handler :show-profile show-profile)
