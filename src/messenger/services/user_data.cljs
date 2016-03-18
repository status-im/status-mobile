(ns messenger.services.user-data
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async :refer [<!]]
            [messenger.models.user-data :refer [set-phone-number
                                                save-phone-number
                                                load-phone-number
                                                set-identity
                                                set-confirmation-code]]
            [messenger.models.protocol :refer [current-identity]]
            [messenger.utils.utils :refer [log on-error]]
            [syng-im.utils.logging :as log]))

(defmulti user-data (fn [state id args]
                              id))

(defmethod user-data :user-data/set-phone-number
  [state id phone-number]
  (log/info "handling " id " args = " phone-number)
  (set-phone-number phone-number))

(defmethod user-data :user-data/save-phone-number
  [state id phone-number]
  (log/info "handling " id " args = " phone-number)
  (save-phone-number phone-number))

(defmethod user-data :user-data/load-phone-number
  [state id args]
  (log/debug "handling " id "args = " args)
  (load-phone-number))

(defmethod user-data :user-data/load-identity
  [state id args]
  (log/info "handling " id " args = " args)
  (let [identity (current-identity)]
    (set-identity identity)))

(defmethod user-data :user-data/set-confirmation-code
  [state id confirmation-code]
  (log/info "handling " id " args = " confirmation-code)
  (set-confirmation-code confirmation-code))

(defn user-data-handler [state [id args]]
  (log/debug "user-data-handler: " args)
  (user-data state id args))
