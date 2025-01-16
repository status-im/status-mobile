(ns status-im.contexts.wallet.common.asset-list.view
  (:require
    [quo.core :as quo]
    [react-native.gesture :as gesture]
    [status-im.contexts.wallet.common.utils :as utils]
    [utils.re-frame :as rf]))

(defn- asset-component
  [{token-symbol  :symbol
    token-name    :name
    total-balance :total-balance
    disabled?     :bridge-disabled?
    :as           token}
   _ _
   {:keys [currency currency-symbol on-token-press preselected-token-symbol prices-per-token]}]
  (let [fiat-value       (utils/calculate-token-fiat-value
                          {:currency         currency
                           :balance          total-balance
                           :token            token
                           :prices-per-token prices-per-token})
        crypto-formatted (utils/get-standard-crypto-format token total-balance prices-per-token)
        fiat-formatted   (utils/fiat-formatted-for-ui currency-symbol fiat-value)]
    [quo/token-network
     {:token       token-symbol
      :label       token-name
      :token-value (str crypto-formatted " " token-symbol)
      :fiat-value  fiat-formatted
      :networks    (seq (:networks token))
      :on-press    #(on-token-press token)
      :state       (cond
                     disabled? :disabled
                     (= preselected-token-symbol token-symbol)
                     :selected)}]))

(defn view
  [{:keys [content-container-style search-text on-token-press preselected-token-symbol chain-ids]
    :or   {content-container-style {:padding-horizontal 8}}}]
  (let [filtered-tokens  (rf/sub [:wallet/current-viewing-account-tokens-filtered
                                  {:query     search-text
                                   :chain-ids chain-ids}])
        currency         (rf/sub [:profile/currency])
        currency-symbol  (rf/sub [:profile/currency-symbol])
        prices-per-token (rf/sub [:wallet/prices-per-token])]
    [gesture/flat-list
     {:data                         filtered-tokens
      :render-data                  {:currency                 currency
                                     :currency-symbol          currency-symbol
                                     :on-token-press           on-token-press
                                     :preselected-token-symbol preselected-token-symbol
                                     :prices-per-token         prices-per-token}
      :style                        {:flex 1}
      :content-container-style      content-container-style
      :keyboard-should-persist-taps :handled
      :key-fn                       :symbol
      :on-scroll-to-index-failed    identity
      :render-fn                    asset-component}]))
