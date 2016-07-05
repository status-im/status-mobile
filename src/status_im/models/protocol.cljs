(ns status-im.models.protocol
  (:require [cljs.reader :refer [read-string]]
            [status-im.protocol.state.storage :as s]
            [status-im.utils.encryption :refer [password-encrypt
                                              password-decrypt]]
            [status-im.utils.types :refer [to-edn-string]]
            [re-frame.db :refer [app-db]]
            [status-im.db :as db]
            [status-im.persistence.simple-kv-store :as kv]))

(defn set-initialized [db initialized?]
  (assoc-in db db/protocol-initialized-path initialized?))

(defn update-identity [db identity]
  (let [identity-string (to-edn-string identity)]
    (s/put kv/kv-store :identity identity-string)
    (assoc db :user-identity identity)))

(defn stored-identity [db]
  (let [identity (s/get kv/kv-store :identity)]
    (when identity
      (read-string identity))))
