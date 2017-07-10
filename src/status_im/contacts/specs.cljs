(ns status-im.contacts.specs
  (:require [cljs.spec.alpha :as s]))

(s/def :contacts/contacts map?)                                     ;; {id (string) contact (map)}
(s/def :contacts/new-contacts seq?)
(s/def :contacts/new-contact-identity string?)                      ;;public key of new contact during adding this new contact
(s/def :contacts/new-contact-public-key-error string?)
(s/def :contacts/contact-identity string?)                          ;;on showing this contact profile
(s/def :contacts/contacts-ui-props map?)
(s/def :contacts/contact-list-ui-props map?)
(s/def :contacts/contacts-click-handler (s/nilable fn?))            ;;used in modal list (for example for wallet)
(s/def :contacts/contacts-click-action (s/nilable keyword?))        ;;used in modal list (for example for wallet)
(s/def :contacts/contacts-click-params map?)                        ;;used in modal list (for example for wallet)


