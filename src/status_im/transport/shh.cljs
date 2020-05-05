(ns ^{:doc "Whisper API and events for managing keys and posting messages"}
 status-im.transport.shh
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.json-rpc :as json-rpc]
            [taoensso.timbre :as log]))

(defn generate-sym-key-from-password
  [waku-enabled? {:keys [password on-success on-error]}]
  (json-rpc/call {:method (if waku-enabled?
                            "waku_generateSymKeyFromPassword"
                            "shh_generateSymKeyFromPassword")
                  :params [password]
                  :on-success on-success
                  :on-error on-error}))

(defn get-sym-key
  [waku-enabled? {:keys [sym-key-id on-success on-error]}]
  (json-rpc/call {:method (if waku-enabled?
                            "waku_getSymKey"
                            "shh_getSymKey")
                  :params [sym-key-id]
                  :on-success on-success
                  :on-error on-error}))

(defn log-error [error]
  (log/error :shh/get-new-sym-key-error error))

(re-frame/reg-fx
 :shh/generate-sym-key-from-password
 (fn [[waku-enabled? {:keys [password on-success]}]]
   (generate-sym-key-from-password
    waku-enabled?
    {:password   password
     :on-success (fn [sym-key-id]
                   (get-sym-key waku-enabled?
                                {:sym-key-id sym-key-id
                                 :on-success (fn [sym-key]
                                               (on-success sym-key sym-key-id))
                                 :on-error log-error}))
     :on-error   log-error})))
