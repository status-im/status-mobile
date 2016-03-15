(ns messenger.comm.intercom
  (:require [cljs.core.async :as async :refer [put!]]
            [messenger.state :refer [state
                                     pub-sub-publisher]]
            [syng-im.utils.logging :as log]))

(defn publish! [topic message]
  (let [publisher (->> (state)
                       (pub-sub-publisher))]
    (put! publisher [topic message])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn load-user-phone-number []
  ;; :service [service_name action_id args_map]
  (publish! :service [:user-data :user-data/load-phone-number nil]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

