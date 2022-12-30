(ns status-im2.contexts.quo-preview.wallet.network-breakdown
  (:require [clojure.string :as string]
            [quo2.components.wallet.network-breakdown :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [i18n.i18n :as i18n]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Ethereum Value"
    :key   :top-value
    :type  :text}
   {:label "Conversion"
    :key   :conversion
    :type  :text}
   {:label "Network"
    :key   :network
    :type  :text}
   {:label   "Icon"
    :key     :icon
    :type    :select
    :options [{:key   :main-icons/arbitrum
               :value "Arbitrum"}
              {:key   :main-icons/optimism
               :value "Optimism"}
              {:key   :main-icons/zksync
               :value "ZK Sync"}
              {:key   :main-icons/arbitrum
               :value "Arbitrum"}
              {:key   :main-icons/ethereum
               :value "Ethereum"}]}])

(defn cool-preview
  []
  (let [state (reagent/atom {:icon       :main-icons/arbitrum
                             :network    "Mainnet"
                             :conversion "5.1234"
                             :top-value  "10 ETH"
                             :labels     {:eth (i18n/label :t/eth)
                                          :on  (string/lower-case (i18n/label :t/on))}})]
    (fn []
      [rn/view
       {:margin-bottom 50
        :padding       16}
       [rn/view {:flex 1}
        [preview/customizer state descriptor]]
       [rn/view
        {:padding-vertical 60
         :flex-direction   :row
         :justify-content  :center}
        [quo2/network-breakdown @state]]
       [rn/touchable-opacity
        {:style    {:background-color colors/neutral-100
                    :width            100}
         :on-press (fn []
                     (swap! state update-in
                       [:network-conversions]
                       conj
                       {:conversion (:conversion @state)
                        :icon       (:icon @state)
                        :network    (:network @state)}))}
        [rn/text {:style {:color colors/white}} "Add current conversion"]]])))

(defn preview-network-breakdown
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white
                                           colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                      1
     :keyboardShouldPersistTaps :always
     :header                    [cool-preview]
     :key-fn                    str}]])
