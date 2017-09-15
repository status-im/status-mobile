(ns status-im.ui.screens.contacts.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as spec]
            status-im.utils.db
            [clojure.string :as string]
            [status-im.data-store.contacts :as contacts]))

(defn contact-can-be-added? [identity]
  (if (contacts/exists? identity)
    (:pending? (contacts/get-by-id identity))
    true))

;;;; DB

;;Contact

;we can't validate public key, because for dapps whisper-identity is just string
(spec/def :contact/whisper-identity :global/not-empty-string)
(spec/def :contact/name :global/not-empty-string)
(spec/def :contact/address (spec/nilable :global/address))
(spec/def :contact/private-key (spec/nilable string?))
(spec/def :contact/public-key (spec/nilable string?))
(spec/def :contact/photo-path (spec/nilable string?))
(spec/def :contact/status (spec/nilable string?))

(spec/def :contact/last-updated (spec/nilable int?))
(spec/def :contact/last-online (spec/nilable int?))
(spec/def :contact/pending? boolean?)
(spec/def :contact/unremovable? boolean?)

(spec/def :contact/dapp? boolean?)
(spec/def :contact/dapp-url (spec/nilable string?))
(spec/def :contact/dapp-hash (spec/nilable int?))
(spec/def :contact/bot-url (spec/nilable string?))
(spec/def :contact/global-command (spec/nilable map?))
(spec/def :contact/commands (spec/nilable (spec/map-of keyword? map?)))
(spec/def :contact/responses (spec/nilable (spec/map-of keyword? map?)))
(spec/def :contact/commands-loaded? (spec/nilable boolean?))
(spec/def :contact/subscriptions (spec/nilable map?))
;true when contact added using status-dev-cli
(spec/def :contact/debug? boolean?)

(spec/def :contact/contact (allowed-keys
                             :req-un [:contact/name :contact/whisper-identity]
                             :opt-un [:contact/address :contact/private-key :contact/public-key :contact/photo-path
                                      :contact/status :contact/last-updated :contact/last-online :contact/pending?
                                      :contact/unremovable? :contact/dapp? :contact/dapp-url :contact/dapp-hash
                                      :contact/bot-url :contact/global-command :contact/commands-loaded?
                                      :contact/commands :contact/responses :contact/debug? :contact/subscriptions
                                      :contact/fcm-token]))

;;Contact list ui props
(spec/def :contact-list-ui/edit? boolean?)

;;Contacts ui props
(spec/def :contacts-ui/edit? boolean?)


(spec/def :contacts/contacts (spec/nilable (spec/map-of :global/not-empty-string :contact/contact)))
;public key of new contact during adding this new contact
(spec/def :contacts/new-identity (spec/nilable string?))
(spec/def :contacts/new-public-key-error (spec/nilable string?))
;on showing this contact's profile (andrey: better to move into profile ns)
(spec/def :contacts/identity (spec/nilable :global/not-empty-string))
(spec/def :contacts/list-ui-props (spec/nilable (allowed-keys :opt-un [:contact-list-ui/edit?])))
(spec/def :contacts/ui-props (spec/nilable (allowed-keys :opt-un [:contacts-ui/edit?])))
;used in modal list (for example for wallet)
(spec/def :contacts/click-handler (spec/nilable fn?))
;used in modal list (for example for wallet)
(spec/def :contacts/click-action (spec/nilable #{:send :request}))
;used in modal list (for example for wallet)
(spec/def :contacts/click-params (spec/nilable map?))



