(ns syng-im.navigation.handlers
  (:require [re-frame.core :refer [register-handler dispatch debug enrich]]))

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

(defmulti preload-data! (fn [_ [_ view-id]] view-id))
(defmethod preload-data! :default [db _] db)

(register-handler :navigate-to
  (enrich preload-data!)
  (fn [db [_ view-id]]
    (push-view db view-id)))

(register-handler :navigation-replace
  (fn [db [_ view-id]]
    (replace-view db view-id)))

(register-handler :navigate-back
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

(register-handler :show-chat
  (fn [db [_ chat-id nav-type]]
    (let [update-view-id-fn (if (= :replace nav-type) replace-view push-view)]
      (-> db
          (update-view-id-fn :chat)
          (assoc :current-chat-id chat-id)))))

(register-handler :show-contacts
  (fn [db _]
    (push-view db :contact-list)))

(defn clear-new-participants [db]
  (assoc-in db :new-participants #{}))

(register-handler :show-remove-participants
  (fn [db _]
    (-> db
        (push-view :remove-participants)
        clear-new-participants)))

(register-handler :show-add-participants
  (fn [db _]
    (-> db
        (push-view :add-participants)
        clear-new-participants)))

(register-handler :show-profile
  (debug
    (fn [db [_ identity]]
      (let [db (assoc db :contact-identity identity)]
        (dispatch [:navigate-to :profile])
        db))))

(register-handler :show-my-profile
  (fn [db _]
    (dispatch [:navigate-to :my-profile])
    db))
