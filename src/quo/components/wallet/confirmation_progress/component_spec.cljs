(ns quo.components.wallet.confirmation-progress.component-spec
  (:require [quo.core :as quo]
            [test-helpers.component :as h]))

(def ^:private theme :light)

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
    (h/render-with-theme-provider [quo/confirmation-propgress
               (get-test-data {:state   :sending
                               :network :optimism})] theme)
    (h/is-truthy (h/get-by-label-text :progress-box)))

  (h/test "component renders when state is confirmed and network is optimism"
    (h/render-with-theme-provider [quo/confirmation-propgress
               (get-test-data {:state   :confirmed
                               :network :optimism})] theme)
    (h/is-truthy (h/get-by-label-text :progress-box)))

  (h/test "component renders when state is finalising and network is optimism"
    (h/render-with-theme-provider [quo/confirmation-propgress
               (get-test-data {:state   :finalising
                               :network :optimism})] theme)
    (h/is-truthy (h/get-by-label-text :progress-box)))

  (h/test "component renders when state is finalized and network is optimism"
    (h/render-with-theme-provider [quo/confirmation-propgress
               (get-test-data {:state   :finalized
                               :network :optimism})] theme)
    (h/is-truthy (h/get-by-label-text :progress-box)))

  (h/test "component renders when state is error and network is optimism"
    (h/render-with-theme-provider [quo/confirmation-propgress
               (get-test-data {:state   :error
                               :network :optimism})] theme)
    (h/is-truthy (h/get-by-label-text :progress-box)))

  (h/test "component renders when state is sending and network is arbitrum"
    (h/render-with-theme-provider [quo/confirmation-propgress
               (get-test-data {:state   :sending
                               :network :arbitrum})] theme)
    (h/is-truthy (h/get-by-label-text :progress-box)))

  (h/test "component renders when state is confirmed and network is arbitrum"
    (h/render-with-theme-provider [quo/confirmation-propgress
               (get-test-data {:state   :confirmed
                               :network :arbitrum})] theme)
    (h/is-truthy (h/get-by-label-text :progress-box)))

  (h/test "component renders when state is finalising and network is arbitrum"
    (h/render-with-theme-provider [quo/confirmation-propgress
               (get-test-data {:state   :finalising
                               :network :arbitrum})] theme)
    (h/is-truthy (h/get-by-label-text :progress-box)))

  (h/test "component renders when state is finalized and network is arbitrum"
    (h/render-with-theme-provider [quo/confirmation-propgress
               (get-test-data {:state   :finalized
                               :network :arbitrum})] theme)
    (h/is-truthy (h/get-by-label-text :progress-box)))

  (h/test "component renders when state is error and network is arbitrum"
    (h/render-with-theme-provider [quo/confirmation-propgress
               (get-test-data {:state   :error
                               :network :arbitrum})] theme)
    (h/is-truthy (h/get-by-label-text :progress-box)))

  (h/test "component renders when state is pending and network is mainnet"
    (h/render-with-theme-provider [quo/confirmation-propgress (get-test-data {})] theme)
    (h/is-truthy (h/get-by-label-text :mainnet-progress-box)))

  (h/test "component renders when state is sending and network is mainnet"
    (h/render-with-theme-provider [quo/confirmation-propgress (get-test-data {:state :sending})] theme)
    (h/is-truthy (h/get-by-label-text :mainnet-progress-box)))

  (h/test "component renders when state is confirmed and network is mainnet"
    (h/render-with-theme-provider [quo/confirmation-propgress (get-test-data {:state :confirmed})] theme)
    (h/is-truthy (h/get-by-label-text :mainnet-progress-box)))

  (h/test "component renders when state is finalising and network is mainnet"
    (h/render-with-theme-provider [quo/confirmation-propgress (get-test-data {:state :finalising})] theme)
    (h/is-truthy (h/get-by-label-text :mainnet-progress-box)))

  (h/test "component renders when state is finalized and network is mainnet"
    (h/render-with-theme-provider [quo/confirmation-propgress (get-test-data {:state :finalized})] theme)
    (h/is-truthy (h/get-by-label-text :mainnet-progress-box)))

  (h/test "component renders when state is error and network is mainnet"
    (h/render-with-theme-provider [quo/confirmation-propgress (get-test-data {:state :error})] theme)
    (h/is-truthy (h/get-by-label-text :mainnet-progress-box))))
