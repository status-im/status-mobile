(ns status-im.app.events
  (:require
    [status-im.infra.transform :as transform]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))


;; whenever we need to publish notification we override the old and increment id
(rf/reg-event-fx
 :app/notify-user
 (fn [{:keys [db]} [message]]
   (let [last-message-id (get-in db [:app :last-user-notification] 0)]
     {:db (assoc-in db
           [:app :last-user-notification]
           (merge message
                  {:id (inc last-message-id)}))})))

(comment
  (rf/dispatch [:app/notify-user
                {:type :positive
                 :text "This is a test notification10"}])
  (rf/dispatch [:app/notify-user
                {:type :negative
                 :text "This is a test notification2"}])




  (rf/dispatch [:toasts/upsert
                {:type :positive
                 :text "This is a test notification3"}])

)

