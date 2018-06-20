(ns status-im.utils.keychain.events
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.keychain.core :as keychain]))

(defn handle-key-error [event {:keys [error key]}]
  (if (= :weak-key error)
    (log/warn "weak key used, database might not be encrypted properly")
    (log/error "invalid key detected"))
  (re-frame/dispatch (into [] (concat event [key error]))))

(re-frame/reg-fx
 :get-encryption-key
 (fn [event]
   (.. (keychain/get-encryption-key)
       (then #(re-frame/dispatch (conj event %)))
       (catch (partial handle-key-error event)))))
