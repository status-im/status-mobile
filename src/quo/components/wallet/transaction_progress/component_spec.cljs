(ns quo.components.wallet.transaction-progress.component-spec
  (:require [quo.core :as quo]
            [test-helpers.component :as h]))

(defn- get-test-data
  [{:keys [state network]
    :or   {state :pending network :mainnet}}]
  {:title               "Title"
   :tag-name            "Doodle"
   :tag-number          120
   :network             network
   :customization-color :blue
   :networks            [{:network      :mainnet
                          :full-name    "Ethereum"
                          :state        state
                          :counter      0
                          :total-box    85
                          :progress     30
                          :epoch-number "123"}
                         {:network      :optimism
                          :full-name    "Optimism"
                          :state        state
                          :progress     50
                          :epoch-number "123"}
                         {:network      :arbitrum
                          :full-name    "Arbitrum"
                          :state        state
                          :progress     30
                          :epoch-number "123"}]
   :on-press            (fn []
                          (js/alert "Transaction progress item pressed"))})

(h/describe "Transaction Progress"
  (h/test "component renders without props"
    (h/render-with-theme-provider
     [quo/transaction-progress {}])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is pending and network is mainnet"
    (h/render-with-theme-provider
     [quo/transaction-progress (get-test-data {:network :optimism-arbitrum})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is sending and network is optimism-arbitrum"
    (h/render-with-theme-provider
     [quo/transaction-progress
      (get-test-data {:state   :sending
                      :network :optimism-arbitrum})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is confirmed and network is optimism-arbitrum"
    (h/render-with-theme-provider
     [quo/transaction-progress
      (get-test-data {:state   :confirmed
                      :network :optimism-arbitrum})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is finalising and network is optimism-arbitrum"
    (h/render-with-theme-provider
     [quo/transaction-progress
      (get-test-data {:state   :finalising
                      :network :optimism-arbitrum})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is finalized and network is optimism-arbitrum"
    (h/render-with-theme-provider
     [quo/transaction-progress
      (get-test-data {:state   :finalized
                      :network :optimism-arbitrum})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is error and network is optimism-arbitrum"
    (h/render-with-theme-provider
     [quo/transaction-progress
      (get-test-data {:state   :error
                      :network :optimism-arbitrum})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is sending and network is optimism"
    (h/render-with-theme-provider
     [quo/transaction-progress
      (get-test-data {:state   :sending
                      :network :optimism})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is confirmed and network is optimism"
    (h/render-with-theme-provider
     [quo/transaction-progress
      (get-test-data {:state   :confirmed
                      :network :optimism})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is finalising and network is optimism"
    (h/render-with-theme-provider
     [quo/transaction-progress
      (get-test-data {:state   :finalising
                      :network :optimism})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is finalized and network is optimism"
    (h/render-with-theme-provider
     [quo/transaction-progress
      (get-test-data {:state   :finalized
                      :network :optimism})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is error and network is optimism"
    (h/render-with-theme-provider
     [quo/transaction-progress
      (get-test-data {:state   :error
                      :network :optimism})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is sending and network is arbitrum"
    (h/render-with-theme-provider
     [quo/transaction-progress
      (get-test-data {:state   :sending
                      :network :arbitrum})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is confirmed and network is arbitrum"
    (h/render-with-theme-provider
     [quo/transaction-progress
      (get-test-data {:state   :confirmed
                      :network :arbitrum})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is finalising and network is arbitrum"
    (h/render-with-theme-provider
     [quo/transaction-progress
      (get-test-data {:state   :finalising
                      :network :arbitrum})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is finalized and network is arbitrum"
    (h/render-with-theme-provider
     [quo/transaction-progress
      (get-test-data {:state   :finalized
                      :network :arbitrum})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is error and network is arbitrum"
    (h/render-with-theme-provider
     [quo/transaction-progress
      (get-test-data {:state   :error
                      :network :arbitrum})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is pending and network is mainnet"
    (h/render-with-theme-provider
     [quo/transaction-progress (get-test-data {})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is sending and network is mainnet"
    (h/render-with-theme-provider
     [quo/transaction-progress (get-test-data {:state :sending})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is confirmed and network is mainnet"
    (h/render-with-theme-provider
     [quo/transaction-progress (get-test-data {:state :confirmed})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is finalising and network is mainnet"
    (h/render-with-theme-provider
     [quo/transaction-progress (get-test-data {:state :finalising})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is finalized and network is mainnet"
    (h/render-with-theme-provider
     [quo/transaction-progress (get-test-data {:state :finalized})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "component renders when state is error and network is mainnet"
    (h/render-with-theme-provider
     [quo/transaction-progress (get-test-data {:state :error})])
    (h/is-truthy (h/get-by-label-text :transaction-progress)))

  (h/test "mainnet progress box is visible network is mainnet"
    (h/render-with-theme-provider
     [quo/transaction-progress (get-test-data {})])
    (h/is-truthy (h/get-by-label-text :mainnet-progress-box)))

  (h/test "arbitrum-optimism progress box is visible network is optimism-arbitrum"
    (h/render-with-theme-provider
     [quo/transaction-progress (get-test-data {:network :optimism-arbitrum})])
    (h/is-truthy (h/get-all-by-label-text :progress-box)))

  (h/test "arbitrum progress box is visible network is arbitrum"
    (h/render-with-theme-provider
     [quo/transaction-progress (get-test-data {:network :arbitrum})])
    (h/is-truthy (h/get-all-by-label-text :progress-box)))

  (h/test "optimism progress box is visible network is optimism"
    (h/render-with-theme-provider
     [quo/transaction-progress (get-test-data {:network :optimism})])
    (h/is-truthy (h/get-all-by-label-text :progress-box)))

  (h/test "title is visible network is optimism-arbitrum"
    (h/render-with-theme-provider
     [quo/transaction-progress (get-test-data {:network :optimism-arbitrum})])
    (h/is-truthy (h/get-by-text "Title")))

  (h/test "title is visible network is mainnet"
    (h/render-with-theme-provider
     [quo/transaction-progress (get-test-data {})])
    (h/is-truthy (h/get-by-text "Title")))

  (h/test "title is visible network is optimism"
    (h/render-with-theme-provider
     [quo/transaction-progress (get-test-data {:network :optimism})])
    (h/is-truthy (h/get-by-text "Title")))

  (h/test "title is visible network is arbitrum"
    (h/render-with-theme-provider
     [quo/transaction-progress (get-test-data {:network :arbitrum})])
    (h/is-truthy (h/get-by-text "Title")))

  (h/test "context tag is visible network is optimism-arbitrum"
    (h/render-with-theme-provider
     [quo/transaction-progress (get-test-data {:network :optimism-arbitrum})])
    (h/is-truthy (h/get-by-label-text :context-tag)))

  (h/test "context tag is visible network is mainnet"
    (h/render-with-theme-provider
     [quo/transaction-progress (get-test-data {})])
    (h/is-truthy (h/get-by-label-text :context-tag)))

  (h/test "context tag is visible network is optimism"
    (h/render-with-theme-provider
     [quo/transaction-progress (get-test-data {:network :optimism})])
    (h/is-truthy (h/get-by-label-text :context-tag)))

  (h/test "context tag is visible network is optimism"
    (h/render-with-theme-provider
     [quo/transaction-progress (get-test-data {:network :arbitrum})])
    (h/is-truthy (h/get-by-label-text :context-tag))))
