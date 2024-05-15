(ns status-im.contexts.preview.quo.counter.step
  (:require
    [quo.components.counter.step.view :refer [?schema]]
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]
    [status-im.contexts.preview.quo.preview-generator :as preview-gen]))

(def descriptor (preview-gen/schema->descriptor ?schema))

(defn view
  []
  (let [state (reagent/atom {:value         "5"
                             :type          :neutral
                             :in-blur-view? false})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:in-blur-view? @state)
        :show-blur-background? (:in-blur-view? @state)}
       [quo/step (dissoc @state :value) (:value @state)]])))
