(ns status-im.contexts.preview.quo.colors.color
  (:require
    [quo.core :as quo]
    [utils.reagent :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [(preview/customization-color-option {:feng-shui? true})
   {:key  :selected?
    :type :boolean}
   {:key  :blur?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:selected?           false
                             :customization-color :blue
                             :blur?               false
                             :on-press            #(js/alert "pressed")})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true
        :blur-dark-only?       true}
       [quo/color (assoc @state :color (:customization-color @state))]])))
