(ns status-im.contexts.preview.quo.wallet.token-input
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as resources]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.common.controlled-input.utils :as controlled-input]
    [status-im.contexts.preview.quo.preview :as preview]
    [status-im.contexts.wallet.common.utils :as utils]
    [utils.money :as money]
    [utils.number :as number]))

(def networks
  [{:source (resources/get-network :arbitrum)}
   {:source (resources/get-network :optimism)}
   {:source (resources/get-network :ethereum)}])

(def title "Max: 200 SNT")

(def descriptor
  [{:key     :token
    :type    :select
    :options [{:key :eth}
              {:key :snt}]}
   {:key     :currency
    :type    :select
    :options [{:key "$"}
              {:key "€"}]}
   {:key  :error?
    :type :boolean}
   {:key  :allow-selection?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:token               :eth
                             :currency            "$"
                             :conversion-rate     3450.28
                             :networks            networks
                             :title               title
                             :customization-color :blue
                             :show-keyboard?      false
                             :allow-selection?    true
                             :crypto?             true})]
    (fn []
      (let [{:keys [currency token conversion-rate
                    crypto?]}             @state
            [input-state set-input-state] (rn/use-state controlled-input/init-state)
            input-amount                  (controlled-input/input-value input-state)
            swap-between-fiat-and-crypto  (fn []
                                            (set-input-state
                                             (fn [input-state]
                                               (controlled-input/set-input-value
                                                input-state
                                                (let [new-value
                                                      (if-not crypto?
                                                        (utils/cut-crypto-decimals-to-fit-usd-cents
                                                         conversion-rate
                                                         (money/fiat->crypto input-amount
                                                                             conversion-rate))
                                                        (utils/cut-fiat-balance-to-two-decimals
                                                         (money/crypto->fiat input-amount
                                                                             conversion-rate)))]
                                                  (number/remove-trailing-zeroes
                                                   new-value))))))
            converted-value               (if crypto?
                                            (utils/prettify-balance currency
                                                                    (money/crypto->fiat input-amount
                                                                                        conversion-rate))
                                            (utils/prettify-crypto-balance
                                             (or (clj->js token) "")
                                             (money/fiat->crypto input-amount conversion-rate)
                                             conversion-rate))]
        [preview/preview-container
         {:state                     state
          :descriptor                descriptor
          :full-screen?              true
          :component-container-style {:flex            1
                                      :justify-content :space-between}}
         [quo/token-input
          (merge @state
                 {:value           input-amount
                  :converted-value converted-value
                  :on-swap         (fn [crypto]
                                     (swap! state assoc :crypto? crypto)
                                     (swap-between-fiat-and-crypto))})]
         [quo/numbered-keyboard
          {:container-style {:padding-bottom (safe-area/get-top)}
           :left-action     :dot
           :delete-key?     true
           :on-press        (fn [c]
                              (set-input-state #(controlled-input/add-character % c)))

           :on-delete       (fn []
                              (set-input-state controlled-input/delete-last))}]]))))
