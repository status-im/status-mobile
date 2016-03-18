(ns messenger.models.user-data
  (:require-macros
   [natal-shell.async-storage :refer [get-item set-item]])
  (:require [cljs.core.async :as async :refer [chan put! <! >!]]
            [syng-im.protocol.web3 :as web3]
            [messenger.state :as state]
            [messenger.utils.utils :refer [log on-error toast]]))

(defn set-phone-number [phone-number]
  (swap! state/app-state assoc :user-phone-number phone-number))

(defn save-phone-number [phone-number]
  (set-item "user-phone-number" phone-number)
  (swap! state/app-state assoc :user-phone-number phone-number))

(defn load-phone-number []
  (get-item "user-phone-number"
            (fn [error value]
              (if error
                (on-error error)
                (swap! state/app-state assoc :user-phone-number (when value
                                                                  (str value)))))))

(defn set-identity [identity]
  (swap! state/app-state assoc :user-identity identity))

(defn set-confirmation-code [code]
  (swap! state/app-state assoc :confirmation-code code))
