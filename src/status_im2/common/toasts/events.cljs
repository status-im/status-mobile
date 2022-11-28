(ns status-im2.common.toasts.events
  (:require
   [status-im.utils.fx :as fx]
   [taoensso.encore    :as enc]))

(def ^:private next-toast-id (atom 0))

(fx/defn upsert
  {:events [:toasts/upsert]}
  [{:keys [db]} id opts]
  (let [{:toasts/keys [index toasts]} db
        update?                       (some #{id} index)
        index                         (enc/conj-when index (and (not update?) id))
        toasts                        (assoc toasts id (assoc opts :update? update?))]
    (enc/assoc-when
     {:db (-> db
              (assoc
               :toasts/index index
               :toasts/toasts toasts)
              (dissoc :toasts/hide-toasts-timer-set))}
     :show-toasts
     (and (not update?) (= (count index) 1) []))))

(fx/defn create
  {:events [:toasts/create]}
  [{:keys [db]} opts]
  {:dispatch [:toasts/upsert (str "toast-" (swap! next-toast-id inc)) opts]})

(fx/defn hide-toasts-with-check
  {:events [:toasts/hide-with-check]}
  [{:keys [db]}]
  (when (:toasts/hide-toasts-timer-set db)
    {:db          (dissoc db :toasts/hide-toasts-timer-set)
     :hide-toasts nil}))

(fx/defn close
  {:events [:toasts/close]}
  [{:keys [db]} id]
  (when (get-in db [:toasts/toasts id])
    (let [{:toasts/keys [toasts index]} db
          toast                         (dissoc toasts id)
          index                         (remove #{id} index)
          empty-index?                  (not (seq index))]
      (enc/assoc-when
       {:db (enc/assoc-when
             db
             :toasts/index  index
             :toasts/toasts toasts
             :toasts/hide-toasts-timer-set empty-index?)}
       :dispatch-later (and empty-index? [{:ms 500 :dispatch [:toasts/hide-with-check]}])))))
