(ns status-im.ui.screens.wallet.send.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as spec]))

(spec/def ::amount (spec/nilable string?))
(spec/def ::to-address (spec/nilable string?))
(spec/def ::to-name (spec/nilable string?))
(spec/def ::amount-error (spec/nilable string?))
(spec/def ::password (spec/nilable string?))
(spec/def ::wrong-password? (spec/nilable boolean?))
(spec/def ::transaction-id (spec/nilable string?))
(spec/def ::waiting-signal? (spec/nilable boolean?))
(spec/def ::signing? (spec/nilable boolean?))
(spec/def ::later? (spec/nilable boolean?))
(spec/def ::height double?)
(spec/def ::width double?)
(spec/def ::camera-dimensions (spec/keys :req-un [::height ::width]))
(spec/def ::camera-flashlight #{:on :off})

(spec/def :wallet/send-transaction (allowed-keys
                                     :opt-un [::amount ::to-address ::to-name ::amount-error ::password ::wrong-password?
                                              ::waiting-signal? ::signing? ::transaction-id ::later? ::camera-dimensions ::camera-flashlight]))
