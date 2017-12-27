(ns status-im.ui.screens.offline-messaging-settings.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as spec]))

(def enode-address-regex #"enode://[a-zA-Z0-9]+\@\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b:(\d{1,5})")

(spec/def ::not-blank-string (spec/and string? seq))
(spec/def :wnode/address (spec/and string? #(re-matches enode-address-regex %)))
(spec/def :wnode/name ::not-blank-string)
(spec/def :inbox/topic ::not-blank-string)
(spec/def :inbox/password ::not-blank-string)
(spec/def :inbox/wnode (allowed-keys :req-un [:wnode/address :wnode/name]))
