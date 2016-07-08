(ns status-im.contacts.validations
  (:require [cljs.spec :as s]
            [status-im.persistence.realm :as realm]))

(defn unique-identity? [identity]
  (not (realm/exists? :contacts :whisper-identity identity)))

(defn valid-length? [identity]
  (= 132 (count identity)))

(s/def ::identity-length valid-length?)
(s/def ::unique-identity unique-identity?)
(s/def ::not-empty-string (s/and string? not-empty))
(s/def ::name ::not-empty-string)
(s/def ::whisper-identity (s/and ::not-empty-string
                                 ::unique-identity
                                 ::identity-length))

(s/def ::contact (s/keys :req-un [::name ::whisper-identity]
                         :opt-un [::phone ::photo-path ::address]))
