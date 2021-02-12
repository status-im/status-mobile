(ns status-im.ui.components.invite.utils
  (:require [status-im.ethereum.tokens :as tokens]
            [re-frame.core :as re-frame]))

;;TODO: this is not cool, because this should be separate subscription
(defn transform-tokens [{:keys [tokens eth-amount]}]
  (let [all-tokens @(re-frame/subscribe [:wallet/all-tokens])
        chain      @(re-frame/subscribe [:ethereum/chain-keyword])]
    (cond-> (mapv (fn [[k v] i]
                    [(tokens/address->token all-tokens k) v i])
                  tokens
                  (range))

      (pos? eth-amount)
      (conj [(get tokens/all-native-currencies chain) eth-amount (count tokens)]))))
