(ns status-im2.contexts.quo-preview.wallet.network-amount
  (:require [clojure.string :as string]
            [quo2.components.wallet.network-amount :refer [network-amount]]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [utils.i18n :as i18n]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def ^:private networks
  [{:icon :i/ethereum :name "Mainnet"}
   {:icon :i/arbitrum :name "Arbitrum"}
   {:icon :i/optimism :name "Optimism"}
   {:icon :i/zksync :name "zkSync"}])

(defn- networks->options
  [networks]
  (for [{:keys [name]
         :as   network}
        networks]
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

(defn- state->opts
  [{:keys [eth-value network show-right-border?]
    :or   {eth-value          5.123456
           show-right-border? true}}]
  {:network-name       (:name (or network default-network))
   :icon               (:icon (or network default-network))
   :eth-value          eth-value
   :show-right-border? show-right-border?})

(defn- cool-preview
  []
  (let [state (reagent/atom {:labels {:eth (i18n/label :t/eth)
                                      :on  (string/lower-case (i18n/label :t/on))}})]
    (fn []
      [rn/view
       {:margin-bottom 50
        :padding       16}
       [preview/customizer state descriptors]
       [rn/view
        {:padding-vertical 60
         :align-items      :center}
        [network-amount (state->opts @state)]]])))

(defn preview
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
