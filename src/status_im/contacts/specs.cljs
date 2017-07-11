(ns status-im.contacts.specs
  (:require [cljs.spec.alpha :as s]))

(s/def :contacts/contacts (s/nilable map?))                                     ;; {id (string) contact (map)}
(s/def :contacts/new-contacts (s/nilable seq?))
(s/def :contacts/new-contact-identity (s/nilable string?))                      ;;public key of new contact during adding this new contact
(s/def :contacts/new-contact-public-key-error (s/nilable string?))
(s/def :contacts/contact-identity (s/nilable string?))                          ;;on showing this contact profile
(s/def :contacts/contacts-ui-props (s/nilable map?))
(s/def :contacts/contact-list-ui-props (s/nilable map?))
(s/def :contacts/contacts-click-handler (s/nilable fn?))            ;;used in modal list (for example for wallet)
(s/def :contacts/contacts-click-action (s/nilable keyword?))        ;;used in modal list (for example for wallet)
(s/def :contacts/contacts-click-params (s/nilable map?))                        ;;used in modal list (for example for wallet)


