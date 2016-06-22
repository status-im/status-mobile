(ns status-im.contacts.validations
  (:require [cljs.spec :as s]
            [status-im.persistence.realm :as realm]))

(s/def ::not-empty-string (s/and string? not-empty))
(defn unique-identity? [identity]
  (println identity)
  (not (realm/exists? :contacts :whisper-identity identity)))

(s/def ::unique-identity unique-identity?)
(s/def ::name ::not-empty-string)
(s/def ::whisper-identity (s/and ::not-empty-string ::unique-identity))

(s/def ::contact (s/keys :req-un [::name ::whisper-identity]
                         :opt-un [::phone ::photo-path ::address]))
