(ns status-im.handlers.server
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.utils.utils :refer [on-error http-post]]
            [taoensso.timbre :as log]))

(defn sign-up
  [db phone-number handler]
  (let [current-account-id (get db :current-account-id)
        {:keys [public-key address]} (get-in db [:accounts current-account-id])]
    ;(user-data/save-phone-number phone-number)
    (log/debug "signing up with public-key" public-key "and phone " phone-number)
    (http-post "sign-up" {:phone-number     phone-number
                          :whisper-identity public-key
                          :address          address}
               (fn [body]
                 (log/debug body)
                 (handler)))
    db))

(defn sign-up-confirm
  [confirmation-code handler]
  (http-post "sign-up-confirm"
             {:code confirmation-code}
             handler))
