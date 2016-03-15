(ns messenger.services.user-data
  (:require [messenger.models.user-data :refer [load-phone-number]]
            [syng-im.utils.logging :as log]))

(defmulti user-data (fn [state id args]
                              id))

(defmethod user-data :user-data/load-phone-number
  [state id args]
  (log/info "handling " id "args = " args)
  (load-phone-number))

(defn user-data-handler [state [id args]]
  (log/info "user notification: " args)
  (user-data state id args))
