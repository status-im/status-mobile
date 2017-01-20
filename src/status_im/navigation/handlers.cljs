(ns status-im.navigation.handlers
  (:require [re-frame.core :refer [dispatch debug enrich after]]
            [status-im.utils.handlers :refer [register-handler]]))

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

(defn -preload-data! [{:keys [was-modal?] :as db} & args]
  (if was-modal?
    (dissoc db :was-modal)
    (apply preload-data! db args)))

(register-handler :navigate-forget
  (enrich preload-data!)
  (fn [db [_ new-view-id]]
    (assoc db :view-id new-view-id)))

(register-handler :navigate-to
  (enrich preload-data!)
  (fn [{:keys [view-id] :as db} [_ new-view-id]]
    (if (= view-id new-view-id)
      db
      (push-view db new-view-id))))

(register-handler :navigate-to-modal
  (enrich preload-data!)
  (fn [db [_ modal-view]]
    (assoc db :modal modal-view)))

(register-handler :navigation-replace
  (enrich preload-data!)
  (fn [db [_ view-id]]
    (replace-view db view-id)))

(register-handler :navigate-back
  (enrich -preload-data!)
  (fn [{:keys [navigation-stack view-id modal] :as db} _]
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
          (assoc db :view-id first-in-stack))))))

(register-handler :navigate-to-tab
  (enrich preload-data!)
  (fn [db [_ view-id]]
    (-> db
        (assoc :prev-tab-view-id (:view-id db))
        (assoc :prev-view-id (:view-id db))
        (push-view view-id))))

(register-handler :on-navigated-to-tab
  (enrich preload-data!)
  (fn [db [_]]
    (assoc db :prev-tab-view-id nil)))

(defn show-profile
  [db [_ identity]]
  (dispatch [:navigate-forget :profile])
  (assoc db :contact-identity identity))

(register-handler :show-profile show-profile)

(defn navigate-to-clean
  [db [_ view-id]]
  (-> db
      (assoc :navigation-stack (list))
      (push-view view-id)))

(register-handler :navigate-to-clean navigate-to-clean)
