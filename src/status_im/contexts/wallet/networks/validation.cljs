(ns status-im.contexts.wallet.networks.validation
  (:require [malli.core]
            [malli.error]))

(def ?network
  [:map {:closed? true}
   [:chain-id int?]
   [:related-chain-id int?]
   [:layer int?]
   [:short-name string?]
   [:abbreviated-name string?]
   [:full-name string?]
   [:network-name string?]
   [:source :schema.common/image-source]
   [:tx-details-base-url string?]
   [:chain-explorer-name string?]
   [:chain-explorer-base-url string?]])

(defn validate-network
  [network]
  (when-not (malli.core/validate ?network network)
    (throw (ex-info "Invalid network data"
                    {:errors (-> (malli.core/explain ?network network)
                                 malli.error/humanize)}))))
