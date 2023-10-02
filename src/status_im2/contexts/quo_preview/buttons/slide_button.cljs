(ns status-im2.contexts.quo-preview.buttons.slide-button
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :size
    :type    :select
    :options [{:key :size/s-48}
              {:key :size/s-40}]}
   {:key  :disabled?
    :type :boolean}
   (preview/customization-color-option {:key :color})])

(defn view
  []
  (let [state     (reagent/atom {:disabled? false
                                 :color     :blue
                                 :size      :size/s-48})
        color     (reagent/cursor state [:color])
        complete? (reagent/atom false)]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:align-items :center}}
       (if (not @complete?)
         [quo/slide-button
          {:track-text          "We gotta slide"
           :track-icon          :face-id
           :customization-color @color
           :size                (:size @state)
           :disabled?           (:disabled? @state)
           :on-complete         (fn []
                                  (js/setTimeout (fn [] (reset! complete? true))
                                                 1000)
                                  (js/alert "I don't wanna slide anymore"))}]
         [quo/button {:on-press (fn [] (reset! complete? false))}
          "Try again"])])))
