(ns status-im2.common.toasts.events
  (:require [utils.re-frame :as rf]))

(rf/defn upsert
  {:events [:toasts/upsert]}
  [{:keys [db]} opts]
  (let [{:keys [ordered toasts]} (:toasts db)
        next-toast-number        (get-in db [:toasts :next-toast-number] 1)
        id                       (or (:id opts)
                                     (str "toast-" next-toast-number))
        update?                  (some #(= % id) ordered)
        ordered                  (if (not update?)
                                   (conj ordered id)
                                   ordered)
        toasts                   (assoc toasts id (dissoc opts :id))]
    (cond-> {:db (-> db
                     (update :toasts assoc :ordered ordered :toasts toasts)
                     (update :toasts dissoc :hide-toasts-timer-set))}

            (and (not update?) (= (count ordered) 1))
            (assoc :show-toasts [])

            (not (:id opts))
            (update-in [:db :toasts :next-toast-number] inc))))

(rf/defn hide-toasts-with-check
  {:events [:toasts/hide-with-check]}
  [{:keys [db]}]
  (when (get-in db [:toasts :hide-toasts-timer-set])
    {:db          (update db :toasts dissoc :hide-toasts-timer-set)
     :hide-toasts nil}))

(rf/defn close
  {:events [:toasts/close]}
  [{:keys [db]} id]
  (when (get-in db [:toasts :toasts id])
    (let [{:keys [toasts ordered]} (:toasts db)
          toasts                   (dissoc toasts id)
          ordered                  (remove #(= % id) ordered)
          empty-ordered?           (not (seq ordered))
          db                       (update db :toasts assoc :ordered ordered :toasts toasts)
          effect                   {:db db}]
      (if empty-ordered?
        (-> effect
            (update-in [:db :toasts] assoc :hide-toasts-timer-set true)
            (assoc :dispatch-later [{:ms 500 :dispatch [:toasts/hide-with-check]}]))
        effect))))
