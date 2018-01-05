(ns status-im.ui.screens.wallet.request.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as spec]
            [status-im.utils.money :as money]))

(spec/def ::amount (spec/nilable money/valid?))
(spec/def ::amount-error (spec/nilable string?))
(spec/def ::symbol (spec/nilable keyword?))

(spec/def :wallet/request-transaction (allowed-keys
                                        :opt-un [::amount ::amount-error ::symbol]))