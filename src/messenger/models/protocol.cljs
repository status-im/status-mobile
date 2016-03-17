(ns messenger.models.protocol
  (:require [messenger.state :as state]
            [messenger.persistence.realm :as r]))

(defn set-initialized [initialized?]
  (swap! state/app-state assoc-in state/protocol-initialized-path initialized?))

;; TODO at least the private key has to be encrypted with user's password

(defn update-identity [identity]
  (r/write
    (fn []
      (r/create :kv-store {:key   :identity
                           :value (str identity)} true))))

(defn current-identity []
  (-> (r/get-by-field :kv-store :key :identity)
      (r/single-cljs)
      (r/decode-value)))
