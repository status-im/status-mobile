(ns status-im.contexts.preview-screens.quo-preview.colors.color
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

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
