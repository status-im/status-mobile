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
  {:profile/profile        {:currency :usd}
   :wallet/network-details [{:source           525
                             :short-name       "eth"
                             :network-name     :ethereum
                             :chain-id         1
                             :related-chain-id 5}]
   :wallet                 {:ui {:send {:token {:symbol                    "ETH"
                                                :decimals                  18
                                                :total-balance             1
                                                :total-balance-fiat        10
                                                :loading-suggested-routes? false
                                                :route                     {}}}}}})

(h/describe "Send > input amount screen"
  (h/test "Default render"
    (setup-subs sub-mocks)
    (h/render [input-amount/view {}])
    (h/is-truthy (h/get-by-text "0"))
    (h/is-truthy (h/get-by-text "ETH"))
    (h/is-truthy (h/get-by-text "$0.00"))
    (h/is-disabled (h/get-by-label-text :button-one)))

  (h/test "Fill token input and confirm"
    (setup-subs sub-mocks)
    (let [on-confirm (h/mock-fn)]
      (h/render [input-amount/view
                 {:on-confirm on-confirm
                  :rate       10}])

      (h/fire-event :press (h/query-by-label-text :keyboard-key-1))
      (h/fire-event :press (h/query-by-label-text :keyboard-key-2))
      (h/fire-event :press (h/query-by-label-text :keyboard-key-3))
      (h/fire-event :press (h/query-by-label-text :keyboard-key-.))
      (h/fire-event :press (h/query-by-label-text :keyboard-key-4))
      (h/fire-event :press (h/query-by-label-text :keyboard-key-5))

      (h/wait-for #(h/is-truthy (h/get-by-text "$1234.50")))

      (h/is-truthy (h/get-by-label-text :button-one))

      (h/fire-event :press (h/get-by-label-text :button-one))
      (h/was-called on-confirm)))

  (h/test "Try to fill more than limit"
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
    (h/wait-for #(h/is-truthy (h/get-by-text "$2850.00"))))

  (h/test "Switch from crypto to fiat and check limit"
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
    (h/wait-for #(h/is-truthy (h/get-by-text "205.50 ETH")))))
