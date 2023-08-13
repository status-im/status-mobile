(ns quo.components.tooltip
  (:require [oops.core :refer [oget]]
            [quo.animated :as animated]
            [quo.design-system.colors :as colors]
            [quo.design-system.spacing :as spacing]
            [quo.platform :as platform] ;; FIXME(Ferossgp): Dependecy on status
            [quo.react-native :as rn]
            [reagent.core :as reagent]
            [status-im.ui.components.icons.icons :as icons]))

(def ^:private initial-height 22)

(defn tooltip-style
  [{:keys [bottom-value animation]}]
  (merge
   (:base spacing/padding-horizontal)
   {:position    :absolute
    :align-items :center
    :left        0
    :right       0
    :top         (- bottom-value)
    :opacity     animation
    :transform   [{:translateY (animated/mix animation 10 0)}]}))

(defn container-style
  []
  {:z-index        2
   :align-items    :center
   :shadow-radius  16
   :shadow-opacity 1
   :shadow-color   (:shadow-01 @colors/theme)
   :shadow-offset  {:width 0 :height 4}})

(defn content-style
  []
  (merge (:base spacing/padding-horizontal)
         {:padding-vertical 6
          :elevation        2
          :background-color (:ui-background @colors/theme)
          :border-radius    8}))

(defn tooltip
  []
  (let [layout      (reagent/atom {:height initial-height})
        ;commented out to upgrade react-native-reanimated to v3 and react-native to 0.72
        ;TODO: replace this with an updated implementation
        ;       animation-v (animated/value 0)
        animation-v 0
        animation   (animated/with-timing-transition
                     animation-v
                     {:easing :deprecated
                      ;                      (:ease-in animated/easings)
                     })
        on-layout   (fn [evt]
                      (let [width  (oget evt "nativeEvent" "layout" "width")
                            height (oget evt "nativeEvent" "layout" "height")]
                        (reset! layout {:width  width
                                        :height height})))]
    (fn [{:keys [bottom-value accessibility-label]} & children]
      [:<>
       ;commented out to upgrade react-native-reanimated to v3 and react-native to 0.72
       ;TODO: replace this with an updated implementation
       ;;;; Animated.Code is deprecated with reanimated version 1.
       ;       [animated/code
       ;        {:exec (animated/cond* (animated/not* animation-v)
       ;                               (animated/set animation-v 1))}]
       [animated/view
        {:style          (tooltip-style {:bottom-value (- (get @layout :height)
                                                          bottom-value)
                                         :animation    animation})
         :pointer-events :box-none}
        [animated/view
         {:style          (container-style)
          :pointer-events :box-none}
         (into [rn/view
                {:style               (content-style)
                 :pointer-events      :box-none
                 :accessibility-label accessibility-label
                 :on-layout           on-layout}]
               children)

         (when platform/ios?
           ;; NOTE(Ferossgp): Android does not show elevation for tooltip making it lost on white bg
           [icons/icon :icons/tooltip-tip
            {:width           18
             :height          8
             :container-style {:elevation 3}
             :color           (:ui-background @colors/theme)}])]]])))
