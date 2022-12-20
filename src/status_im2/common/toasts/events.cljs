(ns status-im2.common.toasts.events
  (:require [taoensso.encore :as enc]
            [utils.re-frame :as rf]))

(def ^:private next-toast-id (atom 0))

(rf/defn upsert
  {:events [:toasts/upsert]}
  [{:keys [db]} id opts]
  (let [{:toasts/keys [index toasts]} db
        update?                       (some #{id} index)
        index                         (enc/conj-when index (and (not update?) id))
        toasts                        (assoc toasts id opts)
        db                            (-> db
                                          (assoc
                                           :toasts/index  index
                                           :toasts/toasts toasts)
                                          (dissoc :toasts/hide-toasts-timer-set))]
    (if (and (not update?) (= (count index) 1))
      {:show-toasts []
       :db          db}
      {:db db})))

(rf/defn create
  {:events [:toasts/create]}
  [{:keys [db]} opts]
  {:dispatch [:toasts/upsert (str "toast-" (swap! next-toast-id inc)) opts]})

(rf/defn hide-toasts-with-check
  {:events [:toasts/hide-with-check]}
  [{:keys [db]}]
  (when (:toasts/hide-toasts-timer-set db)
    {:db          (dissoc db :toasts/hide-toasts-timer-set)
     :hide-toasts nil}))

(rf/defn close
  {:events [:toasts/close]}
  [{:keys [db]} id]
  (when (get-in db [:toasts/toasts id])
    (let [{:toasts/keys [toasts index]} db
          toasts                        (dissoc toasts id)
          index                         (remove #{id} index)
          empty-index?                  (not (seq index))
          db                            (assoc db :toasts/index index :toasts/toasts toasts)]
      (cond-> {:db db}
        empty-index? (update :db assoc :toasts/hide-toasts-timer-set true)
        empty-index? (assoc :dispatch-later [{:ms 500 :dispatch [:toasts/hide-with-check]}])))))
