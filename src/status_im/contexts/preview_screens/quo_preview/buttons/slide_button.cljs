(ns status-im.contexts.preview-screens.quo-preview.buttons.slide-button
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :size
    :type    :select
    :options [{:key :size-48}
              {:key :size-40}]}
   {:key     :type
    :type    :select
    :options [{:key :default}
              {:key :danger}]}
   {:key  :disabled?
    :type :boolean}
   {:key  :blur?
    :type :boolean}
   (preview/customization-color-option {:key :color})])

(defn f-view
  []
  (let [state     (reagent/atom {:disabled? false
                                 :color     :blue
                                 :size      :size-48})
        color     (reagent/cursor state [:color])
        blur?     (reagent/cursor state [:blur?])
        complete? (reagent/atom false)]
    (fn []
      (rn/use-effect (fn []
                       (reset! complete? true)
                       (js/setTimeout #(reset! complete? false) 50))
                     [(:size @state)])
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style (when-not @blur? (:align-items :center))
        :blur?                     @blur?
        :show-blur-background?     true}
       (if (not @complete?)
         [quo/slide-button
          {:track-text          "We gotta slide"
           :track-icon          :face-id
           :customization-color @color
           :size                (:size @state)
           :disabled?           (:disabled? @state)
           :blur?               @blur?
           :type                (:type @state)
           :on-complete         (fn []
                                  (js/setTimeout (fn [] (reset! complete? true))
                                                 1000)
                                  (js/alert "I don't wanna slide anymore"))}]
         [quo/button {:on-press (fn [] (reset! complete? false))}
          "Try again"])])))

(defn view [] [:f> f-view])
