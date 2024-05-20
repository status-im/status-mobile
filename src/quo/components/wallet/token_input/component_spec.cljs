(ns quo.components.wallet.token-input.component-spec
  (:require
    [quo.components.wallet.token-input.view :as token-input]
    [test-helpers.component :as h]))

(h/describe "Wallet: Token Input"
  (h/test "Token label renders"
    (h/render-with-theme-provider [token-input/view
                                   {:token           :snt
                                    :currency        :eur
                                    :currency-symbol "€"
                                    :conversion      1}])
    (h/is-truthy (h/get-by-text "SNT")))

  (h/test "Amount renders"
    (h/render-with-theme-provider [token-input/view
                                   {:token           :snt
                                    :currency        :eur
                                    :currency-symbol "€"
                                    :conversion      1}])
    (h/is-truthy (h/get-by-text "€0.00"))))
