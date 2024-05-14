(ns status-im.contexts.preview.quo.tags.collectible-tag
  (:require
    [quo.components.tags.collectible-tag.schema :refer [?schema]]
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview.quo.preview :as preview]
    [status-im.contexts.preview.quo.preview-generator :as preview-gen]))

(def descriptor (preview-gen/schema->descriptor ?schema))

(defn view
  []
  (let [state (reagent/atom {:size                :size-24
                             :collectible-name    "Collectible"
                             :collectible-id      "#123"
                             :collectible-img-src (resources/mock-images :collectible)
                             :blur?               false})]
    (fn []
      [preview/preview-container
       {:state                 state
        :blur?                 (:blur? @state)
        :show-blur-background? true
        :descriptor            descriptor}
       [rn/view {:style {:align-items :center}}
        [quo/collectible-tag @state]]])))
