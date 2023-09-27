(ns status-im2.contexts.quo-preview.buttons.composer-button
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key  :blur?
    :type :boolean}
   {:key  :disabled?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:icon          :i/placeholder
                             :blur?         false
                             :on-press      #(js/alert "pressed")
                             :on-long-press #(js/alert "long pressed")})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true}
       [quo/composer-button @state]])))
