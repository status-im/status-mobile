(ns status-im.handlers.server
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.utils.utils :refer [http-post]]
            [taoensso.timbre :as log]
            [status-im.utils.scheduler :as sch]))

(defn sign-up
  [db phone-number message-id handler]
  (let [current-account-id (get db :accounts/current-account-id)
        {:keys [public-key address]} (get-in db [:accounts/accounts current-account-id])]
    (log/debug "signing up with public-key" public-key "and phone " phone-number)
    (http-post "sign-up" {:phone-number     phone-number
                          :whisper-identity public-key
                          :address          address}
               (fn [body]
                 (log/debug body)
                 (dispatch [:set-message-status message-id :seen])
                 (handler))
               (fn [_]
                 (sch/execute-later
                   #(dispatch [:sign-up phone-number message-id])
                   (sch/s->ms 1))))
    db))

(defn sign-up-confirm
  [confirmation-code message-id handler]
  (http-post "sign-up-confirm"
             {:code confirmation-code}
             (fn [body]
               (dispatch [:set-message-status message-id :seen])
               (handler body))
             (fn [_]
               (sch/execute-later
                 #(dispatch [:sign-up-confirm confirmation-code message-id])
                 (sch/s->ms 1)))))
