(ns status-im.contacts.validations
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as str]
            [status-im.data-store.contacts :as contacts]))

(def web3 (js/require "web3"))

(defn is-address? [s]
  (.isAddress web3.prototype s))

(defn contact-can-be-added? [identity]
  (if (contacts/exists? identity)
    (:pending? (contacts/get-by-id identity))
    true))

(defn hex-string? [s]
  (let [s' (if (str/starts-with? s "0x")
             (subs s 2)
             s)]
    (boolean (re-matches #"(?i)[0-9a-f]+" s'))))

(defn valid-length? [identity]
  (let [length (count identity)]
    (and
      (hex-string? identity)
      (or
        (and (= 128 length) (not (str/includes? identity "0x")))
        (and (= 130 length) (str/starts-with? identity "0x"))
        (and (= 132 length) (str/starts-with? identity "0x04"))
        (is-address? identity)))))

(s/def ::identity-length valid-length?)
(s/def ::contact-can-be-added contact-can-be-added?)
(s/def ::not-empty-string (s/and string? not-empty))
(s/def ::name ::not-empty-string)
(s/def ::whisper-identity (s/and ::not-empty-string
                                 ::identity-length))

(s/def ::contact (s/keys :req-un [::name ::whisper-identity]
                         :opt-un [::phone ::photo-path ::address]))
