(ns status-im.contexts.wallet.networks.validation
  (:require [malli.core]
            [malli.error]))

(def ?network
  [:map-of
   int?
   [:map {:closed true}
    [:network-name keyword?]
    [:source :schema.common/image-source]
    [:abbreviated-name string?]
    [:block-explorer-name string?]]])

(defn validate-network
  [network]
  (when-not (malli.core/validate ?network network)
    (throw (ex-info "Invalid network data"
                    {:errors (-> (malli.core/explain ?network network)
                                 malli.error/humanize)}))))
