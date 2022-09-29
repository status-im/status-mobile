(ns quo2.screens.wallet.network-amount
  (:require [quo.previews.preview :as preview]
            [quo.react-native :as rn]
            [quo2.components.wallet.network-amount :refer [network-amount]]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]))

(def ^:private networks
  [{:icon :main-icons2/ethereum :name "Mainnet"}
   {:icon :main-icons2/arbitrum :name "Arbitrum"}
   {:icon :main-icons2/optimism :name "Optimism"}
   {:icon :main-icons2/zksync :name "zkSync"}])

(defn- networks->options [networks]
  (for [{:keys [name]
         :as   network} networks]
    {:key   network
     :value name}))

(def ^:private descriptors
  [{:label "ETH Value"
    :key   :eth-value
    :type  :text}
   {:label   "Network"
    :key     :network
    :type    :select
    :options (networks->options networks)}])

(def ^:private default-network
  (first networks))

(defn- state->opts [{:keys [eth-value network show-right-border?]
                     :or   {eth-value          5.123456
                            show-right-border? true}}]
  {:network-name       (:name (or network default-network))
   :icon               (:icon (or network default-network))
   :eth-value          eth-value
   :show-right-border? show-right-border?})

(defn- cool-preview []
  (let [state (reagent/atom nil)]
    (fn []
      [rn/view {:margin-bottom 50
                :padding       16}
       [preview/customizer state descriptors]
       [rn/view {:padding-vertical 60
                 :align-items      :center}
        [network-amount (state->opts @state)]]])))

(defn preview []
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                         1
                  :keyboard-should-persist-taps :always
                  :header                       [cool-preview]
                  :key-fn                       str}]])
