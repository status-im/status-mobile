(ns status-im.handlers.server
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.utils.utils :refer [log on-error http-post]]
            [status-im.utils.logging :as log]))

(defn sign-up
  [db phone-number handler]
  ;(user-data/save-phone-number phone-number)
  (http-post "sign-up" {:phone-number phone-number
                        :whisper-identity (get-in db [:user-identity :public])}
             (fn [body]
               (log body)
               (handler)))
  db)

(defn sign-up-confirm
  [confirmation-code handler]
  (http-post "sign-up-confirm"
             {:code confirmation-code}
             handler))
