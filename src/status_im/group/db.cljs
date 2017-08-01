(ns status-im.group.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as spec]
            [status-im.constants :refer [console-chat-id wallet-chat-id]]
            [clojure.string :as string]
            [status-im.utils.homoglyph :as utils]))

(spec/def ::not-empty-string (spec/and string? not-empty))

(spec/def ::name ::not-empty-string)
;;;; DB

(spec/def :group/group-id ::not-empty-string)
(spec/def :group/name ::not-empty-string)
(spec/def :group/timestamp int?)
(spec/def :group/pending? boolean?)
(spec/def :group/order int?)

(spec/def :group-contact/identity ::not-empty-string)

(spec/def :group/contact (allowed-keys :req-un [:group-contact/identity]))

(spec/def :group/contacts (spec/nilable (spec/* :group/contact)))

(spec/def :group/contact-group (allowed-keys
                                 :req-un [:group/group-id :group/name :group/timestamp
                                          :group/order :group/contacts]
                                 :opt-un [:group/pending?]))

(spec/def :group/contact-groups (spec/nilable (spec/map-of ::not-empty-string :group/contact-group)))
;;used during editing contact group
(spec/def :group/contact-group-id (spec/nilable string?))
(spec/def :group/group-type (spec/nilable #{:chat-group :contact-group}))
(spec/def :group/selected-contacts (spec/nilable (spec/* string?)))
;;list of group ids
(spec/def :group/groups-order (spec/nilable (spec/* string?)))