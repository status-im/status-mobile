(ns status-im.new-group.specs
  (:require [cljs.spec.alpha :as s]))

(s/def :group/contact-groups map?)                               ;; {id (string) group (map)}
(s/def :group/contact-group-id string?)                          ;;used during editing contact group
(s/def :group/group-type keyword?)                               ;;contact group or chat group
(s/def :group/new-group map?)                                    ;;used during creating or edeting contact group
(s/def :group/new-groups (s/nilable vector?))                    ;;used during creating or edeting contact groups
(s/def :group/contacts-group (s/nilable map?))
(s/def :group/selected-contacts set?)
(s/def :group/groups-order seq?)                                 ;;list of group ids