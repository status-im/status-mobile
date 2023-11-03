(ns status-im2.contexts.wallet.send.input-amount.component-spec
  (:require
   [status-im2.contexts.wallet.send.input-amount.view :as input-amount]
   [test-helpers.component :as h]))

(h/describe "Send > input amount screen"
            (h/test "Default render"
                    (h/render [input-amount/view {}])
                    (h/is-truthy (h/get-by-text "0"))
                    (h/is-truthy (h/get-by-text "ETH"))
                    (h/is-truthy (h/get-by-text "$0.00"))
                    (h/is-disabled (h/get-by-label-text :button-one)))

            (h/test "Fill token input and confirm"
                    (let [on-press (h/mock-fn)
                          view (h/render [input-amount/view {:on-confirm on-press}])]
                      (h/fire-event :press (h/query-by-label-text :keyboard-key-1))
                      (h/fire-event :press (h/query-by-label-text :keyboard-key-2))
                      (h/fire-event :press (h/query-by-label-text :keyboard-key-3))
                      (h/fire-event :press (h/query-by-label-text :keyboard-key-.))
                      (h/fire-event :press (h/query-by-label-text :keyboard-key-4))
                      (h/fire-event :press (h/query-by-label-text :keyboard-key-5))
                       (prn (h/debug (h/get-by-label-text :token-input)))
                      (prn (h/debug (.-props (h/get-by-label-text :token-input))))
                      (h/is-truthy (.-props (h/get-by-label-text :token-input)))
                                           
                      (h/is-truthy (h/get-by-label-text :button-one))

                      (h/fire-event :press (h/get-by-label-text :button-one))
                      (h/was-called on-press)))

            (h/test "Try to fill more than limit"
                    (h/render [input-amount/view {}])
                    (h/fire-event :press (h/query-by-label-text :keyboard-key-2))
                    (h/fire-event :press (h/query-by-label-text :keyboard-key-9))
                    (h/fire-event :press (h/query-by-label-text :keyboard-key-5))

                    (h/is-truthy (h/get-by-text "295"))

                    (h/fire-event :press (h/query-by-label-text :keyboard-key-delete))
                    (h/fire-event :press (h/query-by-label-text :keyboard-key-8))
                    (h/fire-event :press (h/query-by-label-text :keyboard-key-5))
                    (h/is-truthy (h/get-by-text "2985"))))
