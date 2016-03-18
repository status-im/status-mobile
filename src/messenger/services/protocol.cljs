(ns messenger.services.protocol
  (:require [messenger.models.protocol :refer [set-initialized
                                               update-identity]]
            [messenger.models.messages :refer [save-message]]
            [syng-im.utils.logging :as log]))

(defmulti protocol (fn [state id args]
                     id))

(defmethod protocol :protocol/initialized
  [state id {:keys [identity] :as args}]
  (log/debug "handling " id "args = " args)
  (update-identity identity)
  (set-initialized true))

(defmethod protocol :protocol/save-new-msg
  [state id {:keys [from payload] :as args}]
  (log/debug "handling " id "args = " args)
  (save-message from payload))

(defn protocol-handler [state [id args]]
  (log/debug "protocol-handler: " args)
  (protocol state id args))
