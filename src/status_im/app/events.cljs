(ns status-im.app.events
  (:require
    [status-im.infra.transform :as transform]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))



(rf/reg-event-fx
 :app/notify-user
 (fn [{:keys [db]} [message]]
   {:db (assoc-in db [:app :last-user-notification] message)}))

(rf/reg-event-fx
 :app/set-view-id
 (fn [{:keys [db]} [value]]
   (let [new-db (assoc db :view-id value)]
     (tap> {:in    :set-view-id
            :value value})

     {:db new-db})))

(rf/reg-event-fx
 :app/inc-toast
 (fn [{:keys [db]} [opts]]
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

       #_(and (not update?) (= (count ordered) 1))
       #_(assoc :show-toasts [(:view-id db) (or (:theme opts) (:theme db))])

       #_(not (:id opts))
       #_(update-in [:db :toasts :next-toast-number] inc)))))

(comment
  (rf/dispatch [:app/notify-user
                {:type :positive
                 :text "This is a test notification10"}])
  (rf/dispatch [:app/notify-user
                {:type :negative
                 :text "This is a test notification2"}])

  (rf/dispatch [:app/set-view-id :test1])
  (rf/dispatch [:app/set-view-id :test2])



  (rf/dispatch [:toasts/upsert
                {:type :positive
                 :text "This is a test notification3"}])

  (rf/dispatch [:app/inc-toast
                {:type :positive
                 :text "This is a test notification4"}])

  (rf/dispatch [:app/inc-toast
                {:type :positive
                 :text "This is a test notification5"}])

  (rf/dispatch [:app/inc-toast
                1])

  (rf/sub [:app/last-user-notification]))

