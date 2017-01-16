(ns status-im.contacts.validations
  (:require [cljs.spec :as s]
            [status-im.data-store.contacts :as contacts]))

(def web3 (js/require "web3"))

(defn is-address? [s]
  (.isAddress web3.prototype s))

(defn contact-can-be-added? [identity]
  (if (contacts/exists? identity)
    (:pending? (contacts/get-by-id identity))
    true))

(defn valid-length? [identity]
  (let [length (count identity)]
    (or
      (= 130 length)
      (= 132 length)
      (is-address? identity))))

(s/def ::identity-length valid-length?)
(s/def ::contact-can-be-added contact-can-be-added?)
(s/def ::not-empty-string (s/and string? not-empty))
(s/def ::name ::not-empty-string)
(s/def ::whisper-identity (s/and ::not-empty-string
                                 ::contact-can-be-added
                                 ::identity-length))

(s/def ::contact (s/keys :req-un [::name ::whisper-identity]
                         :opt-un [::phone ::photo-path ::address]))
