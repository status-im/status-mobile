(ns status-im.contexts.wallet.send.input-amount.component-spec
  (:require
   status-im.contexts.wallet.events
   status-im.contexts.wallet.send.events
   [status-im.contexts.wallet.send.input-amount.view :as input-amount]
   [test-helpers.component :as h]
   [utils.debounce :as debounce]
   [utils.re-frame :as rf]))

(set! rf/dispatch #())
(set! debounce/debounce-and-dispatch #())

(def sub-mocks
  {:profile/profile                              {:currency :usd}
   :wallet/network-details                       [{:source           525
                                                   :short-name       "eth"
                                                   :network-name     :ethereum
                                                   :chain-id         1
                                                   :related-chain-id 5}]
   :wallet/current-viewing-account               {:path "m/44'/60'/0'/0/1"
                                                  :emoji "💎"
                                                  :key-uid "0x2f5ea39"
                                                  :address "0x1"
                                                  :wallet false
                                                  :name "Account One"
                                                  :type :generated
                                                  :watch-only? false
                                                  :chat false
                                                  :test-preferred-chain-ids #{5 420 421613}
                                                  :color :purple
                                                  :hidden false
                                                  :prod-preferred-chain-ids #{1 10 42161}
                                                  :network-preferences-names #{:ethereum :arbitrum
                                                                               :optimism}
                                                  :position 1
                                                  :clock 1698945829328
                                                  :created-at 1698928839000
                                                  :operable "fully"
                                                  :mixedcase-address "0x7bcDfc75c431"
                                                  :public-key "0x04371e2d9d66b82f056bc128064"
                                                  :removed false}
   :wallet/wallet-send-token                     {:symbol                     :eth
                                                  :total-balance              100
                                                  :market-values-per-currency {:usd {:price 10}}}
   :wallet/wallet-send-loading-suggested-routes? false
   :wallet/wallet-send-route                     {:route []}
   :wallet/wallet-send-suggested-routes          {:candidates []}})

(h/describe "Send > input amount screen"
            (h/setup-restorable-re-frame)

            (h/test "Default render"
                    (h/setup-subs sub-mocks)
                    (h/render [input-amount/view
                               {:crypto-decimals 2
                                :limit-crypto    250}])
                    (h/is-truthy (h/get-by-text "0"))
                    (h/is-truthy (h/get-by-text "ETH"))
                    (h/is-truthy (h/get-by-text "$0.00"))
                    (h/is-truthy (h/get-by-label-text :container))
                    (h/is-disabled (h/get-by-label-text :button-one)))

            (h/test "Fill token input and confirm"
                    (h/setup-subs sub-mocks)
                    (let [on-confirm (h/mock-fn)]
                      (h/render [input-amount/view
                                 {:on-confirm      on-confirm
                                  :crypto-decimals 10
                                  :limit-crypto    1000}])

                      (h/fire-event :press (h/query-by-label-text :keyboard-key-1))
                      (h/fire-event :press (h/query-by-label-text :keyboard-key-2))
                      (h/fire-event :press (h/query-by-label-text :keyboard-key-3))
                      (h/fire-event :press (h/query-by-label-text :keyboard-key-.))
                      (h/fire-event :press (h/query-by-label-text :keyboard-key-4))
                      (h/fire-event :press (h/query-by-label-text :keyboard-key-5))

                      (-> (h/wait-for #(h/get-by-text "$1234.50"))
                          (.then (fn []
                                   (h/is-truthy (h/get-by-label-text :button-one))
                                   (h/is-truthy (h/get-by-label-text :container))
                                   (h/fire-event :press (h/get-by-label-text :button-one))
                                   (h/was-called on-confirm))))))

            (h/test "Fill token input and confirm"
                    (h/setup-subs sub-mocks)

                    (let [on-confirm (h/mock-fn)]
                      (h/render [input-amount/view
                                 {:crypto-decimals 10
                                  :limit-crypto    1000
                                  :on-confirm      on-confirm}])

                      (h/fire-event :press (h/query-by-label-text :keyboard-key-1))
                      (h/fire-event :press (h/query-by-label-text :keyboard-key-2))
                      (h/fire-event :press (h/query-by-label-text :keyboard-key-3))
                      (h/fire-event :press (h/query-by-label-text :keyboard-key-.))
                      (h/fire-event :press (h/query-by-label-text :keyboard-key-4))
                      (h/fire-event :press (h/query-by-label-text :keyboard-key-5))

                      (-> (h/wait-for #(h/get-by-text "$1234.50"))
                          (.then (fn []
                                   (h/is-truthy (h/get-by-label-text :button-one))
                                   (h/is-truthy (h/get-by-label-text :container))
                                   (h/fire-event :press (h/get-by-label-text :button-one))
                                   (h/was-called on-confirm))))))

            (h/test "Try to fill more than limit"
                    (h/setup-subs sub-mocks)
                    (h/render [input-amount/view
                               {:crypto-decimals 1
                                :limit-crypto    1}])

                    (h/fire-event :press (h/query-by-label-text :keyboard-key-2))
                    (h/fire-event :press (h/query-by-label-text :keyboard-key-9))
                    (h/fire-event :press (h/query-by-label-text :keyboard-key-5))

                    (h/is-truthy (h/get-by-label-text :container-error)))

            (h/test "Switch from crypto to fiat and check limit"
                    (h/setup-subs sub-mocks)
                    (h/render [input-amount/view
                               {:crypto-decimals 1
                                :limit-crypto    1
                                :on-confirm      #()}])

                    (h/fire-event :press (h/query-by-label-text :keyboard-key-9))
                    (h/is-truthy (h/get-by-label-text :container-error))
                    (h/fire-event :press (h/query-by-label-text :reorder))
                   
            (-> (h/wait-for #(h/get-by-text "Max: 1000.00 USD"))
                (.then (fn []
                         (h/wait-for #(h/is-truthy (h/get-by-label-text :container))))))))
