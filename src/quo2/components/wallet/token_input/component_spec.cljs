(ns quo2.components.wallet.token-input.component-spec
  (:require
    [quo2.components.wallet.token-input.view :as token-input]
    [test-helpers.component :as h]))

(h/describe "Wallet: Token Input"
  (h/test "Token label renders"
    (h/render [token-input/view
               {:token      :snt
                :currency   :eur
                :conversion 1}])
    (h/is-truthy (h/get-by-text "SNT")))

  (h/test "Amount renders"
    (h/render [token-input/view
               {:token      :snt
                :currency   :eur
                :conversion 1}])
    (h/is-truthy (h/get-by-text "€0.00"))))
