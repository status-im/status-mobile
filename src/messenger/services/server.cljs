(ns messenger.services.server
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async :refer [chan put! <!]]
            [messenger.utils.utils :refer [log on-error http-post]]
            [messenger.comm.intercom :as intercom :refer [save-user-phone-number]]
            [syng-im.utils.logging :as log]))

(defmulti server (fn [state id args]
                              id))

(defmethod server :server/sign-up
  [state id {:keys [phone-number whisper-identity handler] :as args}]
  (log/info "handling " id " args = " args)
  (save-user-phone-number phone-number)
  (http-post "sign-up" {:phone-number phone-number
                        :whisper-identity whisper-identity}
             (fn [body]
               (log body)
               ;; TODO replace with core.async
               (handler))))

(defmethod server :server/sign-up-confirm
  [state id {:keys [confirmation-code handler] :as args}]
  (log/info "handling " id " args = " args)
  (http-post "sign-up-confirm"
             {:code confirmation-code}
             handler))

(defn server-handler [state [id args]]
  (log/info "contacts handler: " args)
  (server state id args))
