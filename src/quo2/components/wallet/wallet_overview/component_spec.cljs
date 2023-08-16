(ns quo2.components.wallet.wallet-overview.component-spec
  (:require [quo2.components.wallet.wallet-overview.view :as wallet-overview]
            [test-helpers.component :as h]))

(h/describe
  "Wallet overview test"
  (h/test "renders correct balance"
    (h/render [wallet-overview/view
               {:state      :default
                :time-frame :one-week
                :metrics    :positive
                :balance    "€0.01"}])
    (h/is-truthy (h/get-by-text "€0.01"))))
