(ns messenger.comm.services
  (:require
    [syng-im.utils.logging :as log]
    [messenger.services.user-data :refer [user-data-handler]]
    [messenger.services.protocol :refer [protocol-handler]]
    [messenger.services.server :refer [server-handler]]
    [messenger.services.contacts :refer [contacts-handler]]))

(defmulti service (fn [state service-id args]
                    service-id))

(defmethod service :user-data
  [state service-id args]
  (user-data-handler state args))

(defmethod service :server
  [state service-id args]
  (server-handler state args))

(defmethod service :contacts
  [state service-id args]
  (contacts-handler state args))

(defmethod service :protocol
  [state service-id args]
  (protocol-handler state args))

(defn services-handler [state service-id args]
  (log/info "handling " service-id " args = " args)
  (service state service-id args))
