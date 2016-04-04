(ns syng-im.handlers.server
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.models.user-data :as user-data]
            [syng-im.utils.utils :refer [log on-error http-post]]
            [syng-im.utils.logging :as log]))

(defn sign-up
  [phone-number whisper-identity handler]
  (user-data/save-phone-number phone-number)
  (http-post "sign-up" {:phone-number phone-number
                        :whisper-identity (:public whisper-identity)}
             (fn [body]
               (log body)
               (handler))))

(defn sign-up-confirm
  [confirmation-code handler]
  (http-post "sign-up-confirm"
             {:code confirmation-code}
             handler))
