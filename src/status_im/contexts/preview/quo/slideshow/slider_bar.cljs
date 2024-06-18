(ns status-im.contexts.preview.quo.slideshow.slider-bar
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key  :total-amount
    :type :number}
   {:label "Blur (dark only)?"
    :key   :blur?
    :type  :boolean}
   (preview/customization-color-option)])

(defn view
  []
  (let [[state set-state] (rn/use-state {:total-amount        10
                                         :active-index        0
                                         :blur?               false
                                         :customization-color :blue})]
    [preview/preview-container
     {:state                 state
      :set-state             set-state
      :descriptor            descriptor
      :blur?                 (:blur? state)
      :show-blur-background? true
      :blur-dark-only?       true}
     [rn/view
      {:style {:flex-direction  :row
               :justify-content :center
               :gap             10
               :padding         10}}
      [quo/button
       {:type       :outline
        :style      {:disabled true}
        :disabled?  (zero? (:active-index state))
        :icon-only? true
        :on-press   (fn []
                      (set-state (update state :active-index dec)))}
       :i/chevron-left]
      [quo/button
       {:type       :outline
        :disabled?  (= (:active-index state)
                       (dec (:total-amount state)))
        :icon-only? true
        :on-press   (fn []
                      (set-state (update state :active-index inc)))}
       :i/chevron-right]]
     [quo/slider-bar state]]))
