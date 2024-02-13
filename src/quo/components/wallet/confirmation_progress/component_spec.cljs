(ns quo.components.wallet.confirmation-progress.component-spec
  (:require [quo.core :as quo]
            [test-helpers.component :as h]))


(defn- get-test-data
  [{:keys [state network]
    :or   {state :pending network :mainnet}}]
  {:counter             0
   :total-box           85
   :progress-value      10
   :network             network
   :state               state
   :customization-color :blue})

(h/describe "Confirmation Progress"
  (h/test "component renders when state is sending and network is optimism"
    (h/render-with-theme-provider [quo/confirmation-progress
                                   (get-test-data {:state   :sending
                                                   :network :optimism})])
    (h/is-truthy (h/get-by-label-text :progress-box))))

(h/test "component renders when state is confirmed and network is optimism"
  (h/render-with-theme-provider
   [quo/confirmation-progress
    (get-test-data {:state   :confirmed
                    :network :optimism})])
  (h/is-truthy (h/get-by-label-text :progress-box)))

(h/test "component renders when state is finalising and network is optimism"
  (h/render-with-theme-provider
   [quo/confirmation-progress
    (get-test-data {:state   :finalising
                    :network :optimism})])
  (h/is-truthy (h/get-by-label-text :progress-box)))

(h/test "component renders when state is finalized and network is optimism"
  (h/render-with-theme-provider
   [quo/confirmation-progress
    (get-test-data {:state   :finalized
                    :network :optimism})])
  (h/is-truthy (h/get-by-label-text :progress-box)))

(h/test "component renders when state is error and network is optimism"
  (h/render-with-theme-provider
   [quo/confirmation-progress
    (get-test-data {:state   :error
                    :network :optimism})])
  (h/is-truthy (h/get-by-label-text :progress-box)))

(h/test "component renders when state is sending and network is arbitrum"
  (h/render-with-theme-provider
   [quo/confirmation-progress
    (get-test-data {:state   :sending
                    :network :arbitrum})])
  (h/is-truthy (h/get-by-label-text :progress-box)))

(h/test "component renders when state is confirmed and network is arbitrum"
  (h/render-with-theme-provider
   [quo/confirmation-progress
    (get-test-data {:state   :confirmed
                    :network :arbitrum})])
  (h/is-truthy (h/get-by-label-text :progress-box)))

(h/test "component renders when state is finalising and network is arbitrum"
  (h/render-with-theme-provider
   [quo/confirmation-progress
    (get-test-data {:state   :finalising
                    :network :arbitrum})])
  (h/is-truthy (h/get-by-label-text :progress-box)))

(h/test "component renders when state is finalized and network is arbitrum"
  (h/render-with-theme-provider
   [quo/confirmation-progress
    (get-test-data {:state   :finalized
                    :network :arbitrum})])
  (h/is-truthy (h/get-by-label-text :progress-box)))

(h/test "component renders when state is error and network is arbitrum"
  (h/render-with-theme-provider
   [quo/confirmation-progress
    (get-test-data {:state   :error
                    :network :arbitrum})])
  (h/is-truthy (h/get-by-label-text :progress-box)))

(h/test "component renders when state is pending and network is mainnet"
  (h/render-with-theme-provider
   [quo/confirmation-progress (get-test-data {})])
  (h/is-truthy (h/get-by-label-text :mainnet-progress-box)))

(h/test "component renders when state is sending and network is mainnet"
  (h/render-with-theme-provider
   [quo/confirmation-progress (get-test-data {:state :sending})])
  (h/is-truthy (h/get-by-label-text :mainnet-progress-box)))

(h/test "component renders when state is confirmed and network is mainnet"
  (h/render-with-theme-provider
   [quo/confirmation-progress (get-test-data {:state :confirmed})])
  (h/is-truthy (h/get-by-label-text :mainnet-progress-box)))

(h/test "component renders when state is finalising and network is mainnet"
  (h/render-with-theme-provider
   [quo/confirmation-progress (get-test-data {:state :finalising})])
  (h/is-truthy (h/get-by-label-text :mainnet-progress-box)))

(h/test "component renders when state is finalized and network is mainnet"
  (h/render-with-theme-provider
   [quo/confirmation-progress (get-test-data {:state :finalized})])
  (h/is-truthy (h/get-by-label-text :mainnet-progress-box)))

(h/test "component renders when state is error and network is mainnet"
  (h/render-with-theme-provider
   [quo/confirmation-progress
    (get-test-data {:state :error})])
  (h/is-truthy (h/get-by-label-text :mainnet-progress-box)))
