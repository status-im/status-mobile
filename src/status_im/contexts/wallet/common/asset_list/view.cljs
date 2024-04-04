(ns status-im.contexts.wallet.common.asset-list.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.common.utils :as utils]
    [utils.re-frame :as rf]))

(defn- asset-component
  [token _ _ {:keys [currency currency-symbol on-token-press]}]
  (let [token-units      (utils/total-token-units-in-all-chains token)
        crypto-formatted (utils/get-standard-crypto-format token token-units)
        fiat-value       (utils/total-token-fiat-value currency token)
        fiat-formatted   (utils/get-standard-fiat-format crypto-formatted currency-symbol fiat-value)]
    [quo/token-network
     {:token       (:symbol token)
      :label       (:name token)
      :token-value (str crypto-formatted " " (:symbol token))
      :fiat-value  fiat-formatted
      :networks    (seq (:networks token))
      :on-press    #(on-token-press token)}]))

(defn view
  [{:keys [search-text on-token-press]}]
  (let [filtered-tokens (rf/sub [:wallet/tokens-filtered search-text])
        currency        (rf/sub [:profile/currency])
        currency-symbol (rf/sub [:profile/currency-symbol])]
    [rn/flat-list
     {:data                         filtered-tokens
      :render-data                  {:currency        currency
                                     :currency-symbol currency-symbol
                                     :on-token-press  on-token-press}
      :style                        {:flex 1}
      :content-container-style      {:padding-horizontal 8}
      :keyboard-should-persist-taps :handled
      :key-fn                       :symbol
      :on-scroll-to-index-failed    identity
      :render-fn                    asset-component}]))
