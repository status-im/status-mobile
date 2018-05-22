(ns status-im.ui.screens.offline-messaging-settings.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as spec]
            [status-im.utils.money :as money]))

(def enode-address-regex #"enode://[a-zA-Z0-9]+\@\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b:(\d{1,5})")

(spec/def ::not-blank-string (spec/and string? seq))

(spec/def :wnode/address (spec/and string? #(re-matches enode-address-regex %)))
(spec/def :wnode/name ::not-blank-string)
(spec/def :wnode/id ::not-blank-string)

(spec/def ::address :global/address)
(spec/def ::amount (spec/nilable money/valid?))
(spec/def ::symbol (spec/nilable keyword?))

(spec/def :wnode/payment (allowed-keys :req-un [::address ::amount ::symbol]))
(spec/def :wnode/wnode (allowed-keys
                        :req-un [:wnode/address :wnode/name :wnode/id]
                        :opt-un [:wnode/payment]))

(spec/def :inbox/password ::not-blank-string)
(spec/def :inbox/wnodes (spec/nilable (spec/map-of keyword? (spec/map-of :wnode/id :wnode/wnode))))
(spec/def :inbox/sym-key-id string?)
(spec/def :inbox/last-request integer?)
