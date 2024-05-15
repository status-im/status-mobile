(ns status-im.contexts.preview.quo.wallet.required-tokens
  (:require
    [quo.components.wallet.required-tokens.schema :refer [?schema]]
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview.quo.preview :as preview]
    [status-im.contexts.preview.quo.preview-generator :as preview-gen]))

(def descriptor (preview-gen/schema->descriptor ?schema))

(defn view
  []
  (let [state (reagent/atom
               {:type                :token
                :collectible-img-src (resources/mock-images :collectible)
                :collectible-name    "Collectible name"
                :token               "SNT"
                :amount              "100"
                :divider?            false})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/required-tokens @state]])))
