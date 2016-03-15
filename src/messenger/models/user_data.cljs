(ns messenger.models.user-data
  (:require-macros
    [natal-shell.async-storage :refer [get-item set-item]]
    [natal-shell.alert :refer [alert]])
  (:require [messenger.state :as state]))


(defn load-phone-number []
  (get-item "user-phone-number"
            (fn [error value]
              (if error
                (alert (str "error" error))
                (swap! state/app-state assoc :user-phone-number (when value
                                                                  (str value)))))))


