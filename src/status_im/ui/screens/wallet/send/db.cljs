(ns status-im.ui.screens.wallet.send.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as spec]
            [status-im.utils.money :as money]))

(spec/def ::amount (spec/nilable money/valid?))
(spec/def ::to (spec/nilable string?))
(spec/def ::to-name (spec/nilable string?))
(spec/def ::amount-error (spec/nilable string?))
(spec/def ::password (spec/nilable string?))
(spec/def ::wrong-password? (spec/nilable boolean?))
(spec/def ::id (spec/nilable string?))
(spec/def ::waiting-signal? (spec/nilable boolean?))
(spec/def ::signing? (spec/nilable boolean?))
(spec/def ::later? (spec/nilable boolean?))
(spec/def ::height double?)
(spec/def ::width double?)
(spec/def ::camera-flashlight #{:on :off})
(spec/def ::in-progress? boolean?)
(spec/def ::from-chat? (spec/nilable boolean?))
(spec/def ::symbol (spec/nilable keyword?))
(spec/def ::gas (spec/nilable money/valid?))
(spec/def ::gas-price (spec/nilable money/valid?))
(spec/def ::advanced? boolean?)

(spec/def :wallet/send-transaction (allowed-keys
                                     :opt-un [::amount ::to ::to-name ::amount-error ::password
                                              ::waiting-signal? ::signing? ::id ::later?
                                              ::camera-flashlight ::in-progress?
                                              ::wrong-password? ::from-chat? ::symbol ::advanced?
                                              ::gas ::gas-price]))
