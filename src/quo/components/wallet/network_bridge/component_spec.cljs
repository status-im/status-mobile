(ns quo.components.wallet.network-bridge.component-spec
  (:require
    [quo.components.wallet.network-bridge.view :as network-bridge]
    [test-helpers.component :as h]))

(h/describe "Wallet: Network Bridge"
  (h/test "Amount renders"
    (h/render [network-bridge/view
               {:amount  "50 SNT"
                :network :ethereum
                :status  :default}])
    (h/is-truthy (h/get-by-text "50 SNT")))

  (h/test "Network label renders"
    (h/render [network-bridge/view
               {:amount  "50 SNT"
                :network :optimism
                :status  :default}])
    (h/is-truthy (h/get-by-text "Optimism")))

  (h/test "Locked state"
    (h/render [network-bridge/view
               {:amount  "50 SNT"
                :network :optimism
                :status  :locked}])
    (h/is-truthy (h/get-by-label-text :lock)))

  (h/test "Loading state"
    (h/render [network-bridge/view
               {:amount  "50 SNT"
                :network :optimism
                :status  :loading}])
    (h/is-truthy (h/get-by-label-text :loading)))

  (h/test "Disabled state"
    (h/render [network-bridge/view
               {:amount  "50 SNT"
                :network :optimism
                :status  :disabled}])
    (h/has-style (h/get-by-label-text :container) {:opacity 0.3})))
