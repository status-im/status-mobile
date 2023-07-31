(ns quo2.components.wallet.network-amount.component-spec
  (:require [test-helpers.component :as h]
            [quo2.components.wallet.network-amount.view :as network-amount]))

(h/describe "Wallet: Network Amount"
  (h/test "Amount renders"
    (h/render [network-amount/view
               {:amount "5.123"
                :token  :eth}])
    (h/is-truthy (h/get-by-text "5.123 ETH"))))
