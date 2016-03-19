(ns messenger.services.protocol
  (:require [messenger.models.protocol :refer [set-initialized
                                               update-identity]]
            [messenger.comm.intercom :refer [load-user-identity]]
            [syng-im.utils.logging :as log]))

(defmulti protocol (fn [state id args]
                              id))

(defmethod protocol :protocol/initialized
  [state id {:keys [identity] :as args}]
  (log/info "handling " id "args = " args)
  (update-identity identity)
  (load-user-identity)
  (set-initialized true))

(defn protocol-handler [state [id args]]
  (log/debug "protocol-handler: " args)
  (protocol state id args))
