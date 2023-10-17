(ns quo.components.buttons.wallet-ctas.component-spec
  (:require
    [quo.components.buttons.wallet-ctas.view :as wallet-ctas]
    [test-helpers.component :as h]))

(h/describe "Wallet CTAs test"
  (h/test "Buttons render"
    (h/render [wallet-ctas/view])
    (h/is-truthy (h/get-by-label-text :buy))
    (h/is-truthy (h/get-by-label-text :send))
    (h/is-truthy (h/get-by-label-text :receive))
    (h/is-truthy (h/get-by-label-text :bridge))))
