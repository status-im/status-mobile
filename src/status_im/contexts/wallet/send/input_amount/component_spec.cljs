(ns status-im.contexts.wallet.send.input-amount.component-spec
  (:require
    status-im.contexts.wallet.events
    status-im.contexts.wallet.send.events
    [status-im.contexts.wallet.send.input-amount.view :as input-amount]
    [test-helpers.component :as h]
    [utils.debounce :as debounce]
    [utils.money :as money]
    [utils.re-frame :as rf]))

(set! rf/dispatch #())
(set! debounce/debounce-and-dispatch #())

(def sub-mocks
  {:profile/profile                                {:currency :usd}
   :wallet/selected-network-details                [{:source           525
                                                     :short-name       "eth"
                                                     :network-name     :mainnet
                                                     :chain-id         1
                                                     :related-chain-id 5}]
   :wallet/current-viewing-account-address         "0x1"
   :wallet/current-viewing-account                 {:path "m/44'/60'/0'/0/1"
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
                                                    :network-preferences-names #{:mainnet :arbitrum
                                                                                 :optimism}
                                                    :position 1
                                                    :clock 1698945829328
                                                    :created-at 1698928839000
                                                    :operable :fully
                                                    :mixedcase-address "0x7bcDfc75c431"
                                                    :public-key "0x04371e2d9d66b82f056bc128064"
                                                    :removed false}
   :wallet/current-viewing-account-color           :purple
   :wallet/wallet-send-enough-assets?              true
   :wallet/wallet-send-token                       {:symbol             "ETH"
                                                    :networks           [{:source 879
                                                                          :short-name "eth"
                                                                          :network-name :mainnet
                                                                          :abbreviated-name
                                                                          "Eth."
                                                                          :full-name "Mainnet"
                                                                          :chain-id 1
                                                                          :related-chain-id 1
                                                                          :layer 1}]
                                                    :balances-per-chain {1 {:raw-balance
                                                                            (money/bignumber
                                                                             "2500")
                                                                            :has-error false}}
                                                    :total-balance      100
                                                    :available-balance  100}
   :wallet/wallet-send-loading-suggested-routes?   false
   :wallet/wallet-send-route                       [{:from       {:chainid                1
                                                                  :native-currency-symbol "ETH"}
                                                     :to         {:chain-id               1
                                                                  :native-currency-symbol "ETH"}
                                                     :gas-amount "23487"
                                                     :gas-fees   {:base-fee "32.325296406"
                                                                  :max-priority-fee-per-gas "0.011000001"
                                                                  :eip1559-enabled true}}]
   :wallet/wallet-send-suggested-routes            nil
   :wallet/wallet-send-receiver-networks           [1]
   :view-id                                        :screen/wallet.send-input-amount
   :wallet/wallet-send-to-address                  "0x04371e2d9d66b82f056bc128064"
   :profile/currency-symbol                        "$"
   :profile/currency                               :usd
   :wallet/token-by-symbol                         {:symbol             "ETH"
                                                    :total-balance      100
                                                    :available-balance  100
                                                    :balances-per-chain {1 {:raw-balance
                                                                            (money/bignumber
                                                                             "2500")
                                                                            :has-error false}}}
   :wallet/wallet-send-disabled-from-chain-ids     []
   :wallet/wallet-send-from-locked-amounts         {}
   :wallet/wallet-send-from-values-by-chain        {1 (money/bignumber "250")}
   :wallet/wallet-send-to-values-by-chain          {1 (money/bignumber "250")}
   :wallet/wallet-send-sender-network-values       nil
   :wallet/wallet-send-receiver-network-values     nil
   :wallet/wallet-send-network-links               nil
   :wallet/wallet-send-receiver-preferred-networks [1]
   :wallet/wallet-send-enabled-networks            [{:source           879
                                                     :short-name       "eth"
                                                     :network-name     :mainnet
                                                     :abbreviated-name "Eth."
                                                     :full-name        "Mainnet"
                                                     :chain-id         1
                                                     :related-chain-id 1
                                                     :layer            1}]
   :wallet/wallet-send-enabled-from-chain-ids      [1]
   :wallet/send-amount                             nil
   :wallet/wallet-send-tx-type                     :tx/send
   :wallet/wallet-send-fee-fiat-formatted          "$5,00"
   :wallet/sending-collectible?                    false
   :wallet/send-total-amount-formatted             "250 ETH"
   :wallet/total-amount                            (money/bignumber "250")
   :wallet/prices-per-token                        {:ETH {:usd 10}}
   :wallet/bridge-to-network-details               nil
   :wallet/send-amount-fixed                       ""
   :wallet/send-display-token-decimals             5})

(h/describe "Send > input amount screen"
  (h/setup-restorable-re-frame)

  (h/test "Default render"
    (h/setup-subs (assoc sub-mocks :wallet/send-display-token-decimals 2))
    (h/render-with-theme-provider [input-amount/view
                                   {:limit-crypto             (money/bignumber 250)
                                    :initial-crypto-currency? false}])
    (h/is-truthy (h/get-by-text "0"))
    (h/is-truthy (h/get-by-text "USD"))
    (h/is-truthy (h/get-by-text "0 ETH"))
    (h/is-truthy (h/get-by-label-text :container))
    (h/is-disabled (h/get-by-label-text :button-one)))

  (h/test "Fill token input and confirm"
    (h/setup-subs (assoc sub-mocks :wallet/send-display-token-decimals 10))

    (let [on-confirm (h/mock-fn)]
      (h/render-with-theme-provider [input-amount/view
                                     {:limit-crypto             (money/bignumber 1000)
                                      :on-confirm               on-confirm
                                      :initial-crypto-currency? true}])

      (h/fire-event :press (h/query-by-label-text :keyboard-key-1))
      (h/fire-event :press (h/query-by-label-text :keyboard-key-2))
      (h/fire-event :press (h/query-by-label-text :keyboard-key-3))
      (h/fire-event :press (h/query-by-label-text :keyboard-key-.))
      (h/fire-event :press (h/query-by-label-text :keyboard-key-4))
      (h/fire-event :press (h/query-by-label-text :keyboard-key-5))


      (-> (h/wait-for #(h/get-by-text "$1234.50"))
          (.then (fn []
                   (let [btn (h/get-by-label-text :button-one)]
                     (h/is-truthy btn)
                     (h/fire-event :press btn)
                     (h/was-called on-confirm)))))))

  (h/test "Try to fill more than limit"
    (h/setup-subs (assoc sub-mocks :wallet/send-display-token-decimals 1))
    (h/render-with-theme-provider [input-amount/view
                                   {:limit-crypto (money/bignumber 1)}])

    (h/fire-event :press (h/query-by-label-text :keyboard-key-2))
    (h/fire-event :press (h/query-by-label-text :keyboard-key-9))
    (h/fire-event :press (h/query-by-label-text :keyboard-key-5))

    (h/is-truthy (h/get-by-label-text :container-error)))

  (h/test "Switch from crypto to fiat and check limit"
    (h/setup-subs (assoc sub-mocks :wallet/send-display-token-decimals 1))
    (h/render-with-theme-provider [input-amount/view
                                   {:limit-crypto (money/bignumber 10)
                                    :on-confirm   #()}])

    (h/fire-event :press (h/query-by-label-text :keyboard-key-9))
    (h/fire-event :press (h/query-by-label-text :keyboard-key-9))
    (h/is-truthy (h/get-by-label-text :container-error))
    (h/fire-event :press (h/query-by-label-text :reorder))
    (h/is-truthy #(h/get-by-text "Max: $100.00"))))

