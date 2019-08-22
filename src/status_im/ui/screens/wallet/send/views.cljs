(ns status-im.ui.screens.wallet.send.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ethereum.tokens :as tokens]
            [status-im.i18n :as i18n]
            [status-im.ui.components.bottom-buttons.view :as bottom-buttons]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.wallet.components.views :as wallet.components]
            [status-im.ui.screens.wallet.send.styles :as styles]
            [status-im.ui.components.toolbar.actions :as actions]))

(defn- toolbar [title]
  [toolbar/toolbar {:transparent? true}
   [toolbar/nav-button (actions/back-white #(actions/default-handler))]
   [toolbar/content-title {:color :white} title]])

(defn- sign-transaction-button [sign-enabled?]
  [bottom-buttons/bottom-buttons
   styles/sign-buttons
   [react/view]
   [button/button {:style               components.styles/flex
                   :disabled?           (not sign-enabled?)
                   :on-press            #(re-frame/dispatch [:wallet.ui/sign-transaction-button-clicked])
                   :text-style          {:color :white}
                   :accessibility-label :sign-transaction-button}
    (i18n/label :t/transactions-sign-transaction)
    [vector-icons/icon :main-icons/next {:color (if sign-enabled?
                                                  colors/white
                                                  colors/white-transparent-10)}]]])

(defn- render-send-transaction-view [{:keys [chain transaction scroll all-tokens amount-input network-status]}]
  (let [{:keys [from amount amount-text amount-error asset-error to to-name sufficient-funds? symbol]} transaction
        {:keys [decimals] :as token} (tokens/asset-for all-tokens chain symbol)
        online? (= :online network-status)]
    [wallet.components/simple-screen {:avoid-keyboard? true
                                      :status-bar-type :wallet}
     [toolbar (i18n/label :t/send-transaction)]
     [react/view components.styles/flex
      [common/network-info {:text-color :white}]
      [react/scroll-view {:keyboard-should-persist-taps :always
                          :ref                          #(reset! scroll %)
                          :on-content-size-change       #(when (and scroll @scroll)
                                                           (.scrollToEnd @scroll))}
       [react/view styles/send-transaction-form
        [wallet.components/recipient-selector
         {:address   to
          :name      to-name}]
        [wallet.components/asset-selector
         {:error     asset-error
          :address   from
          :type      :send
          :symbol    symbol}]
        [wallet.components/amount-selector
         {:error         (or amount-error (when-not sufficient-funds? (i18n/label :t/wallet-insufficient-funds)))
          :amount        amount
          :amount-text   amount-text
          :input-options {:on-change-text #(re-frame/dispatch [:wallet.send/set-and-validate-amount % symbol decimals])
                          :ref            (partial reset! amount-input)}} token]]]
      [sign-transaction-button (and to
                                    (nil? amount-error)
                                    (not (nil? amount))
                                    sufficient-funds?
                                    online?)]]]))

(defn- send-transaction-view [{:keys [scroll]}]
  (let [amount-input (atom nil)
        handler #(when (and scroll @scroll @amount-input (.isFocused @amount-input)) (.scrollToEnd @scroll))]
    (reagent/create-class
     {:component-did-mount (fn [_]
                             ;;NOTE(goranjovic): keyboardDidShow is for android and keyboardWillShow for ios
                             (.addListener (react/keyboard)  "keyboardDidShow" handler)
                             (.addListener (react/keyboard)  "keyboardWillShow" handler))
      :reagent-render       (fn [opts] (render-send-transaction-view
                                        (assoc opts :amount-input amount-input)))})))

(defview send-transaction []
  (letsubs [transaction [:wallet.send/transaction]
            chain [:ethereum/chain-keyword]
            scroll (atom nil)
            network-status [:network-status]
            all-tokens [:wallet/all-tokens]]
    [send-transaction-view {:transaction    transaction
                            :scroll         scroll
                            :chain          chain
                            :all-tokens     all-tokens
                            :network-status network-status}]))
