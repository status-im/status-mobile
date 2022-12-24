(ns status-im2.common.toasts.events
  (:require
   [utils.re-frame :as rf]))

(rf/defn upsert
  {:events [:toasts/upsert]}
  [{:keys [db]} id opts]
  (let [{:keys [ordered toasts]} (:toasts db)
        update?                  (some #(= % id) ordered)
        ordered                  (if (not update?) (conj ordered id) ordered)
        toasts                   (assoc toasts id opts)
        db                       (-> db
                                     (update :toasts assoc :ordered ordered :toasts toasts)
                                     (update :toasts dissoc :hide-toasts-timer-set))]
    (if (and (not update?) (= (count ordered) 1))
      {:show-toasts []
       :db          db}
      {:db db})))

(rf/defn create
  {:events [:toasts/create]}
  [{:keys [db]} opts]
  (let [next-toast-id (or (get-in [:toasts :next-toast-id] db) 1)]
    {:db       (assoc-in db [:toasts :next-toast-id] (inc next-toast-id))
     :dispatch [:toasts/upsert (str "toast-" next-toast-id) opts]}))

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
