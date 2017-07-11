(ns status-im.new-group.specs
  (:require [cljs.spec.alpha :as s]))

(s/def :group/contact-groups (s/nilable map?))                               ;; {id (string) group (map)}
(s/def :group/contact-group-id (s/nilable string?))                          ;;used during editing contact group
(s/def :group/group-type (s/nilable keyword?))                               ;;contact group or chat group
(s/def :group/new-group (s/nilable map?))                                    ;;used during creating or edeting contact group
(s/def :group/new-groups (s/nilable vector?))                    ;;used during creating or edeting contact groups
(s/def :group/contacts-group (s/nilable map?))
(s/def :group/selected-contacts (s/nilable set?))
(s/def :group/groups-order (s/nilable seq?))                                 ;;list of group ids