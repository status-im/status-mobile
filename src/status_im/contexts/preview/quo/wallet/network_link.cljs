(ns status-im.contexts.preview.quo.wallet.network-link
  (:require
    [quo.components.wallet.network-link.schema :refer [?schema]]
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]
    [status-im.contexts.preview.quo.preview-generator :as preview-gen]))

(def descriptor
  (conj (preview-gen/schema->descriptor ?schema)
        {:key :width :type :number}))

(defn view
  []
  (let [state (reagent/atom {:shape       :linear
                             :source      :ethereum
                             :destination :optimism
                             :width       63})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-top 40
                                    :align-items :center}}
       [rn/view {:style {:width (max (:width @state) 63)}}
        [quo/network-link @state]]])))
