(ns messenger.models.protocol
  (:require [cljs.reader :refer [read-string]]
            [messenger.state :as state]
            [syng-im.protocol.state.storage :as s]
            [syng-im.utils.encryption :refer [password-encrypt
                                              password-decrypt]]
            [messenger.utils.types :refer [to-edn-string]]))

(defn set-initialized [initialized?]
  (swap! state/app-state assoc-in state/protocol-initialized-path initialized?))

(defn update-identity [identity]
  (let [password  (get-in @state/app-state state/identity-password-path)
        encrypted (->> (to-edn-string identity)
                       (password-encrypt password))]
    (s/put (state/kv-store) :identity encrypted)))

(defn current-identity []
  (let [encrypted (s/get (state/kv-store) :identity)
        password  (get-in @state/app-state state/identity-password-path)]
    (when encrypted
      (-> (password-decrypt password encrypted)
          (read-string)))))
