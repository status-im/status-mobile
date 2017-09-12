(ns status-im.ui.screens.wallet.request.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as spec]))

(spec/def ::amount (spec/nilable string?))

(spec/def :wallet/request-transaction (allowed-keys
                                        :opt-un [::amount]))