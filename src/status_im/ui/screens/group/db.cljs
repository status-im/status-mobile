(ns status-im.ui.screens.group.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as spec]
            status-im.utils.db
            [status-im.constants :refer [console-chat-id]]
            [clojure.string :as string]
            [status-im.utils.homoglyph :as utils]))

(spec/def ::name :global/not-empty-string)
;;;; DB

(spec/def :group/group-id :global/not-empty-string)
(spec/def :group/name :global/not-empty-string)
(spec/def :group/timestamp int?)
(spec/def :group/pending? boolean?)
(spec/def :group/order int?)

(spec/def :group/contact :global/not-empty-string)

(spec/def :group/contacts (spec/nilable (spec/* :group/contact)))

(spec/def :group/contact-group (allowed-keys
                                :req-un [:group/group-id :group/name :group/timestamp
                                         :group/order :group/contacts]
                                :opt-un [:group/pending?]))

(spec/def :group/contact-groups (spec/nilable (spec/map-of :global/not-empty-string :group/contact-group)))
;;used during editing contact group
(spec/def :group/contact-group-id (spec/nilable string?))
(spec/def :group/group-type (spec/nilable #{:chat-group :contact-group}))
(spec/def :group/selected-contacts (spec/nilable (spec/* string?)))
;;list of group ids
(spec/def :group/groups-order (spec/nilable (spec/* string?)))
