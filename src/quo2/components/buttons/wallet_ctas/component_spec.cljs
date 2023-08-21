(ns quo2.components.buttons.wallet-ctas.component-spec
  (:require [test-helpers.component :as h]
            [quo2.components.buttons.wallet-ctas.view :as wallet-ctas]))

(h/describe "Wallet CTAs test"
  (h/test "Buttons render"
    (h/render [wallet-ctas/view])
    (h/is-truthy (h/get-by-text "Send"))
    (h/is-truthy (h/get-by-text "Buy"))
    (h/is-truthy (h/get-by-text "Receive"))
    (h/is-truthy (h/get-by-text "Bridge"))))
