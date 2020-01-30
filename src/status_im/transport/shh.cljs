(ns ^{:doc "Whisper API and events for managing keys and posting messages"}
 status-im.transport.shh
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [status-im.transport.utils :as transport.utils]
            [status-im.utils.config :as config]
            [taoensso.timbre :as log]
            [status-im.ethereum.json-rpc :as json-rpc]))

(defn generate-sym-key-from-password
  [{:keys [password on-success on-error]}]
  (json-rpc/call {:method (if config/waku-enabled?
                            "waku_generateSymKeyFromPassword"
                            "shh_generateSymKeyFromPassword")
                  :params [password]
                  :on-success on-success
                  :on-error on-error}))

(defn get-sym-key
  [{:keys [sym-key-id on-success on-error]}]
  (json-rpc/call {:method (if config/waku-enabled?
                            "waku_getSymKey"
                            "shh_getSymKey")
                  :params [sym-key-id]
                  :on-success on-success
                  :on-error on-error}))

(defn log-error [error]
  (log/error :shh/get-new-sym-key-error error))

(re-frame/reg-fx
 :shh/generate-sym-key-from-password
 (fn [args]
   (doseq [{:keys [password on-success]} args]
     (generate-sym-key-from-password {:password   password
                                      :on-success (fn [sym-key-id]
                                                    (get-sym-key {:sym-key-id sym-key-id
                                                                  :on-success (fn [sym-key]
                                                                                (on-success sym-key sym-key-id))
                                                                  :on-error log-error}))
                                      :on-error   log-error}))))
