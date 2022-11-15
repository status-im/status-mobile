(ns status-im2.common.toasts.events
  (:require
   [status-im.utils.fx :as fx]
   [taoensso.encore    :as enc]))

(def ^:private next-toast-id (atom 0))

(fx/defn upsert
  {:events [:toasts/upsert]}
  [{:keys [db]} id opts]
  (let [{:toasts/keys [index toasts]} db
        update? (some #{id} index)
        index   (enc/conj-when index (and (not update?) id))
        toasts  (assoc toasts id (assoc opts :update? update?))]
    (enc/assoc-when
     {:db (assoc db
                 :toasts/index  index
                 :toasts/toasts toasts)}
     :show-toasts
     (and (not update?) (= (count index) 1) []))))

(fx/defn create
  {:events [:toasts/create]}
  [{:keys [db]} opts]
  {:dispatch [:toasts/upsert (str "toast-" (swap! next-toast-id inc)) opts]})

(fx/defn close
  {:events [:toasts/close]}
  [{:keys [db]} id]
  (let [{:toasts/keys [toasts index]} db
        toast (dissoc toasts id)
        index (remove #{id} index)]
    (enc/assoc-when
     {:db (assoc db
                 :toasts/index  index
                 :toasts/toasts toasts)}
     :hide-toasts
     (and (not (seq index)) 500))))
