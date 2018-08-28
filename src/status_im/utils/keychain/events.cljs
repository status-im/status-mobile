(ns status-im.utils.keychain.events
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.keychain.core :as keychain]
            [status-im.utils.platform :as platform]))

(defn handle-key-error [event {:keys [error key]}]
  (if (= :weak-key error)
    (log/warn "weak key used, database might not be encrypted properly")
    (log/warn "invalid key detected"))
  (re-frame/dispatch (into [] (concat event [(or key "")
                                             (or error :invalid-key)]))))

(re-frame/reg-fx
 :keychain/get-encryption-key
 (fn [event]
   (when platform/desktop? (keychain/set-username))
   (.. (keychain/get-encryption-key)
       (then #(re-frame/dispatch (conj event %)))
       (catch (partial handle-key-error event)))))
