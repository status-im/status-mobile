(ns status-im.models.protocol
  (:require [cljs.reader :refer [read-string]]
            [status-im.protocol.state.storage :as s]
            [status-im.utils.types :refer [to-edn-string]]
            [re-frame.db :refer [app-db]]
            [status-im.db :as db]
            [status-im.persistence.simple-kv-store :as kv]))

(defn set-initialized [db initialized?]
  (assoc-in db db/protocol-initialized-path initialized?))

(defn update-identity [db {:keys [address] :as identity}]
  (let [identity-string (to-edn-string identity)]
    (s/put kv/kv-store :identity identity-string)
    (assoc-in db [:accounts address] identity)))

(defn stored-identity [db]
  (let [identity (s/get kv/kv-store :identity)]
    (when identity
      (read-string identity))))
