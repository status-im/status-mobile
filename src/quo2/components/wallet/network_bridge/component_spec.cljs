(ns quo2.components.wallet.network-bridge.component-spec
  (:require [test-helpers.component :as h]
            [quo2.components.wallet.network-bridge.view :as network-bridge]))

(h/describe "Wallet: Network Bridge"
  (h/test "Amount renders"
    (h/render [network-bridge/view
               {:amount  "50 SNT"
                :network :ethereum
                :state   :default}])
    (h/is-truthy (h/get-by-text "50 SNT")))

  (h/test "Network label renders"
    (h/render [network-bridge/view
               {:amount  "50 SNT"
                :network :optimism
                :state   :default}])
    (h/is-truthy (h/get-by-text "Optimism")))

  (h/test "Locked state"
    (h/render [network-bridge/view
               {:amount  "50 SNT"
                :network :optimism
                :state   :locked}])
    (h/is-truthy (h/get-by-label-text :lock)))

  (h/test "Loading state"
    (h/render [network-bridge/view
               {:amount  "50 SNT"
                :network :optimism
                :state   :loading}])
    (h/is-truthy (h/get-by-label-text :loading)))

  (h/test "Disabled state"
    (h/render [network-bridge/view
               {:amount  "50 SNT"
                :network :optimism
                :state   :disabled}])
    (h/has-style (h/get-by-label-text :container) {:opacity 0.3})))
