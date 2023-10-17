(ns quo.components.wallet.network-amount.component-spec
  (:require
    [quo.components.wallet.network-amount.view :as network-amount]
    [test-helpers.component :as h]))

(h/describe "Wallet: Network Amount"
  (h/test "Amount renders"
    (h/render [network-amount/view
               {:amount "5.123"
                :token  :eth}])
    (h/is-truthy (h/get-by-text "5.123 ETH"))))
