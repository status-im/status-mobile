(ns status-im.contexts.preview.quo.wallet.progress-bar
  (:require
    [quo.components.wallet.progress-bar.schema :refer [?schema]]
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]
    [status-im.contexts.preview.quo.preview-generator :as preview-gen]))

(def descriptor (preview-gen/schema->descriptor ?schema))

(defn view
  []
  (let [state (reagent/atom {:state               :pending
                             :full-width?         false
                             :progressed-value    "10"
                             :customization-color :blue})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-top 40
                                    :align-items :center}}
       [rn/view {:style {:flex-direction :row}}
        [quo/progress-bar @state]]])))
