(ns status-im.utils.keychain.events
  (:require [re-frame.core :as re-frame]
            [status-im.thread :as status-im.thread]
            [taoensso.timbre :as log]
            [status-im.utils.keychain.core :as keychain]))

(defn handle-key-error [event {:keys [error key]}]
  (if (= :weak-key error)
    (log/warn "weak key used, database might not be encrypted properly")
    (log/warn "invalid key detected"))
  (status-im.thread/dispatch (into [] (concat event [(or key "")
                                                     (or error :invalid-key)]))))

(re-frame/reg-fx
 :get-encryption-key
 (fn [event]
   (.. (keychain/get-encryption-key)
       (then #(re-frame/dispatch (conj event %)))
       (catch (partial handle-key-error event)))))
