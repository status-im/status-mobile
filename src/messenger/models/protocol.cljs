(ns messenger.models.protocol
  (:require [messenger.state :as state]
            [syng-im.protocol.state.storage :as s]))

(defn set-initialized [initialized?]
  (swap! state/app-state assoc-in state/protocol-initialized-path initialized?))

;; TODO at least the private key has to be encrypted with user's password

(defn update-identity [identity]
  (s/put (state/kv-store) :identity identity))

(defn current-identity []
  (s/get (state/kv-store) :identity))
