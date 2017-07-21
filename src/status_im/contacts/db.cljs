(ns status-im.contacts.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as str]
            [status-im.data-store.contacts :as contacts]
            [status-im.js-dependencies :as dependencies]))

(defn contact-can-be-added? [identity]
  (if (contacts/exists? identity)
    (:pending? (contacts/get-by-id identity))
    true))

(defn is-address? [s]
  (.isAddress dependencies/Web3.prototype s))

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

(s/def ::not-empty-string (s/and string? not-empty))
(s/def ::public-key (s/and ::not-empty-string valid-length?))

;;;; DB

;;Contact

;we can't validate public key, because for dapps whisper-identity is just string
(s/def :contact/whisper-identity ::not-empty-string)
(s/def :contact/name ::not-empty-string)
(s/def :contact/address (s/nilable is-address?))
(s/def :contact/private-key (s/nilable string?))
(s/def :contact/public-key (s/nilable string?))
(s/def :contact/photo-path (s/nilable string?))
(s/def :contact/status (s/nilable string?))

(s/def :contact/last-updated (s/nilable int?))
(s/def :contact/last-online (s/nilable int?))
(s/def :contact/pending? boolean?)
(s/def :contact/unremovable? boolean?)

(s/def :contact/dapp? boolean?)
(s/def :contact/dapp-url (s/nilable string?))
(s/def :contact/dapp-hash (s/nilable int?))
(s/def :contact/bot-url (s/nilable string?))
(s/def :contact/global-command (s/nilable map?))
(s/def :contact/commands (s/nilable (s/map-of keyword? map?)))
(s/def :contact/responses (s/nilable (s/map-of keyword? map?)))
(s/def :contact/commands-loaded? (s/nilable boolean?))
(s/def :contact/subscriptions (s/nilable map?))
;true when contact added using status-dev-cli
(s/def :contact/debug? boolean?)

(s/def :contact/contact (allowed-keys
                          :req-un [:contact/name :contact/whisper-identity]
                          :opt-un [:contact/address :contact/private-key :contact/public-key :contact/photo-path
                                   :contact/status :contact/last-updated :contact/last-online :contact/pending?
                                   :contact/unremovable? :contact/dapp? :contact/dapp-url :contact/dapp-hash
                                   :contact/bot-url :contact/global-command :contact/commands-loaded?
                                   :contact/commands :contact/responses :contact/debug? :contact/subscriptions]))

;;Contact list ui props
(s/def :contact-list-ui/edit? boolean?)

;;Contacts ui props
(s/def :contacts-ui/edit? boolean?)


(s/def :contacts/contacts (s/nilable (s/map-of ::not-empty-string :contact/contact)))
;public key of new contact during adding this new contact
(s/def :contacts/new-identity (s/nilable string?))
(s/def :contacts/new-public-key-error (s/nilable string?))
;on showing this contact's profile (andrey: better to move into profile ns)
(s/def :contacts/identity (s/nilable ::not-empty-string))
(s/def :contacts/list-ui-props (s/nilable (allowed-keys :opt-un [:contact-list-ui/edit?])))
(s/def :contacts/ui-props (s/nilable (allowed-keys :opt-un [:contacts-ui/edit?])))
;used in modal list (for example for wallet)
(s/def :contacts/click-handler (s/nilable fn?))
;used in modal list (for example for wallet)
(s/def :contacts/click-action (s/nilable #{:send :request}))
;used in modal list (for example for wallet)
(s/def :contacts/click-params (s/nilable map?))



