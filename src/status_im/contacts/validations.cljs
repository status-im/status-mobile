(ns status-im.contacts.validations
  (:require [cljs.spec :as s]))

(s/def ::not-empty-string (s/and string? not-empty))

(s/def ::name ::not-empty-string)
(s/def ::whisper-identity ::not-empty-string)

(s/def ::contact (s/keys :req-un [::name ::whisper-identity]
                         :opt-un [::phone ::photo-path ::address]))