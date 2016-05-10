(ns syng-im.navigation.handlers
  (:require [re-frame.core :refer [register-handler]]))

(register-handler :navigate-to
  (fn [db [_ view-id]]
    (-> db
        (assoc :view-id view-id)
        (update :navigation-stack conj view-id))))

(register-handler :navigate-back
  (fn [{:keys [navigation-stack] :as db} _]
    (if (>= 1 (count navigation-stack))
      db
      (let [[view-id :as navigation-stack'] (pop navigation-stack)]
        (-> db
            (assoc :view-id view-id)
            (assoc :navigation-stack navigation-stack'))))))
