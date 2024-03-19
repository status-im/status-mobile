(ns status-im.contexts.preview.quo.foundations.gradients
  (:require [quo.foundations.gradients :as quo.gradients]
            [reagent.core :as reagent]
            [status-im.contexts.preview.quo.preview :as preview]))

(def ^:private descriptor
  [{:key     :color-index
    :type    :select
    :options [{:key :gradient-1}
              {:key :gradient-2}
              {:key :gradient-3}
              {:key :gradient-4}
              {:key :gradient-5}]}])

(defn view
  []
  (let [state (reagent/atom {:color-index 1})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? (:blur? @state)}
       [quo.gradients/view
        {:container-style {:align-self :center
                           :height     200
                           :width      200}
         :color-index     (:color-index @state)}]])))
