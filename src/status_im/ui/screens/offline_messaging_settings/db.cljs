(ns status-im.ui.screens.offline-messaging-settings.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require
   [cljs.spec.alpha :as spec]
   [status-im.utils.inbox :as utils.inbox]))

(spec/def ::not-blank-string (spec/and string? seq))

(spec/def :wnode/address (spec/and string? utils.inbox/valid-enode-address?))
(spec/def :wnode/name ::not-blank-string)
(spec/def :wnode/id ::not-blank-string)
(spec/def :wnode/user-defined boolean?)
(spec/def :wnode/password (spec/nilable string?))
(spec/def :wnode/wnode (allowed-keys :req-un [:wnode/address :wnode/name :wnode/id]
                                     :opt-un [:wnode/user-defined :wnode/password]))

(spec/def :inbox/password ::not-blank-string)
(spec/def :inbox/wnodes (spec/nilable (spec/map-of keyword? (spec/map-of :wnode/id :wnode/wnode))))
(spec/def :inbox/sym-key-id string?)
(spec/def :inbox/last-request integer?)
(spec/def :inbox/last-received integer?)
