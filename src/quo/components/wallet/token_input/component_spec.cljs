(ns quo.components.wallet.token-input.component-spec
  (:require
    [quo.components.wallet.token-input.view :as token-input]
    [test-helpers.component :as h]))

(h/describe "Wallet: Token Input"
  (h/test "Token label renders"
    (h/render-with-theme-provider [token-input/view
                                   {:token-symbol    :snt
                                    :currency-symbol :snt
                                    :on-swap         nil
                                    :error?          false
                                    :value           "1"
                                    :converted-value "10"}])
    (h/is-truthy (h/get-by-text "SNT")))

  (h/test "Amount renders"
    (h/render-with-theme-provider [token-input/view
                                   {:token-symbol    :snt
                                    :currency-symbol :snt
                                    :on-swap         nil
                                    :error?          false
                                    :value           "1"
                                    :converted-value "€0.00"}])
    (h/is-truthy (h/get-by-text "€0.00"))))
