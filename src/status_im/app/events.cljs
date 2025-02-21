(ns status-im.app.events
  (:require
    [status-im.infra.transform :as transform]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))


(rf/reg-event-fx
 :app/notify-user
 (fn [{:keys [db]} [{:keys [text type] :as message}]]
   (let [notification-id (-> db
                             (get-in [:app :last-user-notification] 0)
                             inc)]
     {;; whenever we need to publish notification we override the old and increment id
      :db (assoc-in db
           [:app :last-user-notification]
           (merge message
                  {:id notification-id}))
      ;; TODO: toasts are part of ui layer and they shouldn't be published here. Instead some part
      ;; of ui should keep track of last user notification and generate toast
      :fx [[:dispatch
            [:toasts/upsert
             {:type type
              :text text}]]]})))

(comment
  (rf/dispatch [:app/notify-user
                {:type :positive
                 :text "This is a good news"}])
  (rf/dispatch [:app/notify-user
                {:type :negative
                 :text "This is a bad news"}])

  (rf/dispatch [:toasts/upsert
                {:type :positive
                 :text "This is a test notification3"}])
)

