(ns quo2.components.qr-scan-animation
  (:require [quo.design-system.colors :as colors]
            [quo.react :as react]
            [quo.react-native :as rn]
            [quo2.reanimated :as reanimated]
            [status-im.utils.dimensions :as dimensions]))

(defn corner [border1 border2 outside-border] 
  (let [{:keys [width height]} (dimensions/window)]
    [reanimated/view (merge (assoc {:border-color colors/white-persist
                             :width (* 0.1 width)
                             :height (* 0.05 height)
                             :position :absolute}
                            border1 3
                            border2 3)
                            (case outside-border
                              :top-left {:top -3
                                         :left -2}
                              :top-right {:top -3
                                          :right -2}
                              :bottom-left {:bottom -3
                                            :left -2}
                              :bottom-right {:bottom -3
                                             :right -2}))]))

(def viewfinder-port
  {:position        :absolute
   :align-items     :center
   :justify-content :center
   :flex            1})

(defn- viewfinder [size]
  [:f>
   (fn []
     [rn/view {:style viewfinder-port}

      [rn/view {:style {:border-width 700
                        :border-color "rgba(0,0,0,0.7)"}}
       [reanimated/view {:style (reanimated/apply-animations-to-style
                                 {:width size
                                  :height size}
                                 {:width size
                                  :height size
                                  :justify-content :space-between})}
        [rn/view {:flex-direction :row :justify-content :space-between}
         [corner :border-top-width :border-left-width :top-left]
         [corner :border-top-width :border-right-width :top-right]]
        [rn/view {:flex-direction :row :justify-content :space-between}
         [corner :border-bottom-width :border-left-width :bottom-left]
         [corner :border-bottom-width :border-right-width :bottom-right]]]]])])

(defn preview
  []
  [:f>
   (fn []
     (let [{:keys [width]} (dimensions/window) 
           {:keys [min-scale max-scale]} {:min-scale (* width 0.4)
                                          :max-scale (* width 0.8)}
           size (reanimated/use-shared-value min-scale)
           difference (- max-scale min-scale)]
       [:<> 
       (react/effect! (fn []
                        (reanimated/animate-shared-value-with-delay size
                                                                    max-scale
                                                                    difference
                                                                    :easing2 200)
                        []))
         [rn/view {:style {:justify-content :center
                           :align-items :center
                           :height "100%"}}
          [viewfinder size]]]))])