(ns messenger.services.server
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async :refer [chan put! <!]]
            [messenger.android.utils :refer [log on-error http-post]]
            [syng-im.utils.logging :as log]))

(defmulti server (fn [state id args]
                              id))

(defmethod server :server/sign-up
  [state id args]
  (log/info "handling " id " args = " args)
  (http-post "sign-up" (select-keys args [:phone-number :whisper-identity])
             (fn [body]
               (log body)
               ;; TODO replace with core.async
               ((:handler args)))))

(defn server-handler [state [id args]]
  (log/info "user notification: " args)
  (server state id args))
