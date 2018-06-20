(ns status-im.ui.screens.offline-messaging-settings.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require
   [status-im.models.mailserver :as models.mailserver]
   [cljs.spec.alpha :as spec]))

(spec/def ::not-blank-string (spec/and string? seq))

(spec/def :wnode/address (spec/and string? models.mailserver/valid-enode-address?))
(spec/def :wnode/name ::not-blank-string)
(spec/def :wnode/id ::not-blank-string)
(spec/def :wnode/user-defined boolean?)
(spec/def :wnode/password ::not-blank-string)
(spec/def :wnode/sym-key-id string?)
(spec/def :wnode/wnode (allowed-keys :req-un [:wnode/address :wnode/name :wnode/id]
                                     :opt-un [:wnode/sym-key-id
                                              :wnode/user-defined
                                              :wnode/password]))

(spec/def :inbox/wnodes (spec/nilable (spec/map-of keyword? (spec/map-of :wnode/id :wnode/wnode))))
(spec/def :inbox/last-received integer?)
