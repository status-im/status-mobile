(ns status-im.contexts.wallet.common.asset-list.view
  (:require
    [quo.core :as quo]
    [react-native.gesture :as gesture]
    [status-im.contexts.wallet.common.utils :as utils]
    [utils.re-frame :as rf]))

(defn- asset-component
  [{token-symbol :symbol
    token-name   :name
    :as          token}
   _ _
   {:keys [currency currency-symbol on-token-press preselected-token-symbol]}]
  (let [token-units      (utils/total-token-units-in-all-chains token)
        crypto-formatted (utils/get-standard-crypto-format token token-units)
        fiat-value       (utils/total-token-fiat-value currency token)
        fiat-formatted   (utils/get-standard-fiat-format crypto-formatted currency-symbol fiat-value)]
    [quo/token-network
     {:token       token-symbol
      :label       token-name
      :token-value (str crypto-formatted " " token-symbol)
      :fiat-value  fiat-formatted
      :networks    (:networks token)
      :on-press    #(on-token-press token)
      :state       (when (= preselected-token-symbol token-symbol)
                     :selected)}]))

(defn view
  [{:keys [content-container-style search-text on-token-press preselected-token-symbol]
    :or   {content-container-style {:padding-horizontal 8}}}]
  (let [filtered-tokens (rf/sub [:wallet/tokens-filtered search-text])
        currency        (rf/sub [:profile/currency])
        currency-symbol (rf/sub [:profile/currency-symbol])]
    [gesture/flat-list
     {:data                         filtered-tokens
      :render-data                  {:currency                 currency
                                     :currency-symbol          currency-symbol
                                     :on-token-press           on-token-press
                                     :preselected-token-symbol preselected-token-symbol}
      :style                        {:flex 1}
      :content-container-style      content-container-style
      :keyboard-should-persist-taps :handled
      :key-fn                       :symbol
      :on-scroll-to-index-failed    identity
      :render-fn                    asset-component}]))
