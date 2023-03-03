(ns status-im2.common.sticky-scroll-view.view
  (:require [react-native.reanimated :as reanimated]
            [react-native.core :as rn]))

(defn sticky-item
  [{:keys [height translation-y background-color]} content]
  [:f>
   (fn []
     [rn/view {:height height :z-index 1}
      [reanimated/view
       {:style (reanimated/apply-animations-to-style
                {:transform [{:translateY translation-y}]}
                {:position         :absolute
                 :top              0
                 :left             0
                 :right            0
                 :height           height
                 :background-color background-color})}
       content]])])

(defn scroll-view
  [{:keys [scroll-y blur]} & content]
  [:f>
   (fn []
     (let [scroll-handler (worklets.scroll-view/use-animated-scroll-handler scroll-y)
           opacity  (when blur
                      (reanimated/interpolate scroll-y
                                              [0 (:delta blur)]
                                              [0 1]
                                              {:extrapolateLeft  "clamp"
                                               :extrapolateRight "extend"}))]
       (into [reanimated/scroll-view
              {:scroll-event-throttle           1
               :shows-vertical-scroll-indicator false
               :contentInsetAdjustmentBehavior  :never
               :on-scroll                       scroll-handler}]
             (concat
              (when blur
                [[reanimated/blur-view
                  {:blur-amount 10
                   :blur-type   :xlight
                   :style       (reanimated/apply-animations-to-style
                                 {:transform [{:translateY scroll-y}]
                                  :opacity opacity}
                                 {:z-index  1
                                  :position :absolute
                                  :top      0
                                  :height   (:height blur)
                                  :right    0
                                  :left     0})}]])
              content))))])
