(ns status-im.contexts.wallet.common.utils.networks
  (:require [clojure.string :as string]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.utils :as utils]))

(defn resolve-receiver-networks
  [{:keys [prefix testnet-enabled? goerli-enabled?]}]
  (let [prefix     (if (string/blank? prefix)
                     constants/default-multichain-address-prefix
                     prefix)
        prefix-seq (string/split prefix #":")]
    (->> prefix-seq
         (remove string/blank?)
         (mapv
          #(utils/network->chain-id
            {:network          %
             :testnet-enabled? testnet-enabled?
             :goerli-enabled?  goerli-enabled?})))))
