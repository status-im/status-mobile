(ns quo.components.wallet.swap-input.component-spec
  (:require [quo.components.wallet.swap-input.view :as swap-input]
            [test-helpers.component :as h]))

(h/describe "Wallet: Swap Input"
  (h/test "should render correctly with props"
    (h/render-with-theme-provider
     [swap-input/view
      {:type              :pay
       :error?            false
       :token             "SNT"
       :status            :default
       :currency-symbol   "€"
       :value             "5"
       :fiat-value        "1.50"
       :network-tag-props {:title "Max: 200 SNT"}}])
    (h/is-truthy (h/get-by-label-text :swap-input))
    (h/is-truthy (h/get-by-text "SNT"))
    (h/is-truthy (h/get-by-text "€1.50"))
    (h/is-truthy (h/get-by-text "Max: 200 SNT")))

  (h/test "should render correctly with approval label"
    (h/render-with-theme-provider
     [swap-input/view
      {:type :pay
       :show-approval-label? true
       :approval-label-props
       {:status       :approve
        :token-value  "10"
        :token-symbol "SNT"}}])
    (h/is-truthy (h/get-by-text "Approve 10 SNT"))))
