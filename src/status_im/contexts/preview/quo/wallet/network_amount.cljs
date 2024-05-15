(ns status-im.contexts.preview.quo.wallet.network-amount
  (:require
    [quo.components.wallet.network-amount.schema :refer [?schema]]
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]
    [status-im.contexts.preview.quo.preview-generator :as preview-gen]))

(def descriptor (preview-gen/schema->descriptor ?schema))

(defn view
  []
  (let [state (reagent/atom {:amount "5.123456"
                             :token  "ETH"})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/network-amount @state]])))
