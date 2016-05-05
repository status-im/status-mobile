(ns syng-im.models.protocol
  (:require [cljs.reader :refer [read-string]]
            [syng-im.protocol.state.storage :as s]
            [syng-im.utils.encryption :refer [password-encrypt
                                              password-decrypt]]
            [syng-im.utils.types :refer [to-edn-string]]
            [re-frame.db :refer [app-db]]
            [syng-im.db :as db]
            [syng-im.persistence.simple-kv-store :as kv]
            [syng-im.utils.logging :as log]))

(defn set-initialized [db initialized?]
  (assoc-in db db/protocol-initialized-path initialized?))

(defn update-identity [db identity]
  (let [password  (get-in db db/identity-password-path)
        encrypted (password-encrypt password (to-edn-string identity))]
    (s/put kv/kv-store :identity encrypted)
    (assoc db :user-identity identity)))

(defn stored-identity [db]
  (let [encrypted (s/get kv/kv-store :identity)
        password  (get-in db db/identity-password-path)]
    (when encrypted
      (read-string (password-decrypt password encrypted)))))

(comment

  (stored-identity @re-frame.db/app-db)
  )
