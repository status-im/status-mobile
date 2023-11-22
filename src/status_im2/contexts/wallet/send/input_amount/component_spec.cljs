(ns status-im2.contexts.wallet.send.input-amount.component-spec
  (:require
    [re-frame.core :as re-frame]
    [status-im2.contexts.wallet.send.input-amount.view :as input-amount]
    [test-helpers.component :as h]))

(defn setup-subs
  [subscriptions]
  (doseq [keyval subscriptions]
    (re-frame/reg-sub
     (key keyval)
     (fn [_] (val keyval)))))

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
   :wallet/wallet-send-token                     {:symbol :eth}
   :wallet/wallet-send-loading-suggested-routes? false
   :wallet/wallet-send-route                     {:route []}})

(h/describe "Send > input amount screen"
  (h/test "Default render"
    (with-redefs [re-frame/dispatch #()]
      (setup-subs sub-mocks)
      (h/render [input-amount/view {}])
      (h/is-truthy (h/get-by-text "0"))
      (h/is-truthy (h/get-by-text "ETH"))
      (h/is-truthy (h/get-by-text "$0.00"))
      (h/is-disabled (h/get-by-label-text :button-one))))

  (h/test "Fill token input and confirm"
    (with-redefs [re-frame/dispatch #()]
      (setup-subs sub-mocks)
      (let [on-confirm (h/mock-fn)]
        (h/render [input-amount/view
                   {:on-confirm on-confirm
                    :rate       10
                    :limit      1000}])

        (h/fire-event :press (h/query-by-label-text :keyboard-key-1))
        (h/fire-event :press (h/query-by-label-text :keyboard-key-2))
        (h/fire-event :press (h/query-by-label-text :keyboard-key-3))
        (h/fire-event :press (h/query-by-label-text :keyboard-key-.))
        (h/fire-event :press (h/query-by-label-text :keyboard-key-4))
        (h/fire-event :press (h/query-by-label-text :keyboard-key-5))

        (h/wait-for #(h/is-truthy (h/get-by-text "$1234.50")))

        (h/is-truthy (h/get-by-label-text :button-one))

        (h/fire-event :press (h/get-by-label-text :button-one))
        (h/was-called on-confirm))))

  (h/test "Try to fill more than limit"
    (with-redefs [re-frame/dispatch #()]
      (setup-subs sub-mocks)
      (h/render [input-amount/view
                 {:rate  10
                  :limit 286}])

      (h/fire-event :press (h/query-by-label-text :keyboard-key-2))
      (h/fire-event :press (h/query-by-label-text :keyboard-key-9))
      (h/fire-event :press (h/query-by-label-text :keyboard-key-5))

      (h/wait-for #(h/is-truthy (h/get-by-text "$290.00")))

      (h/fire-event :press (h/query-by-label-text :keyboard-key-backspace))
      (h/fire-event :press (h/query-by-label-text :keyboard-key-8))
      (h/fire-event :press (h/query-by-label-text :keyboard-key-5))
      (h/wait-for #(h/is-truthy (h/get-by-text "$2850.00")))))

  (h/test "Switch from crypto to fiat and check limit"
    (with-redefs [re-frame/dispatch #()]
      (setup-subs sub-mocks)
      (h/render [input-amount/view
                 {:rate  10
                  :limit 250}])

      (h/fire-event :press (h/query-by-label-text :keyboard-key-2))
      (h/fire-event :press (h/query-by-label-text :keyboard-key-0))
      (h/wait-for #(h/is-truthy (h/get-by-text "$200.00")))

      (h/fire-event :press (h/query-by-label-text :reorder))

      (h/wait-for #(h/is-truthy (h/get-by-text "2.00 ETH")))

      (h/fire-event :press (h/query-by-label-text :keyboard-key-5))
      (h/fire-event :press (h/query-by-label-text :keyboard-key-5))

      (h/wait-for #(h/is-truthy (h/get-by-text "205.50 ETH")))
      (h/fire-event :press (h/query-by-label-text :keyboard-key-5))
      (h/wait-for #(h/is-truthy (h/get-by-text "205.50 ETH"))))))
