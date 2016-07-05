(ns status-im.handlers.server
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.utils.utils :refer [on-error http-post]]
            [status-im.utils.logging :as log]))

(defn sign-up
  [db phone-number handler]
  (let [{:keys [public-key address] :as account} (get-in db [:user-identity])]
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
