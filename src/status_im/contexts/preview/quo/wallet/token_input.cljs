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
    [utils.money :as money]))

(def networks
  [{:source (resources/get-network :arbitrum)}
   {:source (resources/get-network :optimism)}
   {:source (resources/get-network :ethereum)}])

(def title "Max: 200 SNT")
(def conversion-rate 3450.28)

(def descriptor
  [{:key     :token-symbol
    :type    :select
    :options [{:key :eth}
              {:key :snt}]}
   {:key     :currency
    :type    :select
    :options [{:key "$"}
              {:key "€"}]}
   {:key  :error?
    :type :boolean}])


(defn view
  []
  (let [state (reagent/atom {:token-symbol :eth
                             :currency     "$"
                             :crypto?      true
                             :error?       false})]
    (fn []
      (let [{:keys [currency token-symbol crypto? error?]} @state
            [input-state set-input-state]                  (rn/use-state controlled-input/init-state)
            input-amount                                   (controlled-input/input-value input-state)
            swap-between-fiat-and-crypto                   (fn []
                                                             (if crypto?
                                                               (set-input-state #(controlled-input/->fiat
                                                                                  %
                                                                                  conversion-rate))
                                                               (set-input-state
                                                                #(controlled-input/->crypto
                                                                  %
                                                                  conversion-rate))))
            converted-value                                (if crypto?
                                                             (utils/prettify-balance currency
                                                                                     (money/crypto->fiat
                                                                                      input-amount
                                                                                      conversion-rate))
                                                             (utils/prettify-crypto-balance
                                                              (or (clj->js token-symbol) "")
                                                              (money/fiat->crypto input-amount
                                                                                  conversion-rate)
                                                              conversion-rate))]
        [preview/preview-container
         {:state                     state
          :descriptor                descriptor
          :full-screen?              true
          :component-container-style {:flex            1
                                      :justify-content :space-between}}
         [quo/token-input
          {:token-symbol    token-symbol
           :currency-symbol (if crypto? token-symbol currency)
           :error?          error?
           :value           input-amount
           :converted-value converted-value
           :on-swap         (fn []
                              (swap! state assoc :crypto? (not crypto?))
                              (swap-between-fiat-and-crypto))
           :hint-component  [quo/network-tags
                             {:networks networks
                              :title    title
                              :status   (when (:error? @state) :error)}]}]
         [quo/numbered-keyboard
          {:container-style {:padding-bottom (safe-area/get-top)}
           :left-action     :dot
           :delete-key?     true
           :on-press        (fn [c]
                              (set-input-state #(controlled-input/add-character % c)))

           :on-delete       (fn []
                              (set-input-state controlled-input/delete-last))}]]))))
