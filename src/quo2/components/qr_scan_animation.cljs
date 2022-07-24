(ns quo2.components.qr-scan-animation
  (:require [quo.design-system.colors :as colors]
            [quo.react :as react]
            [quo.react-native :as rn]
            [quo2.reanimated :as reanimated]))

(defn corner [border1 border2]
  [reanimated/view (assoc {:border-color colors/white-persist
                           :width 60
                           :height 60}
                          border1 2
                          border2 2)])

(def viewfinder-port
  {:position        :absolute
   :align-items     :center
   :justify-content :center
   :flex            1})

(defn- viewfinder [size]
  [:f>
   (fn []
     [rn/view {:style viewfinder-port}

      [rn/view {:style {:border-width 400
                        :border-color "rgba(0,0,0,0.7)"}}
       [reanimated/view {:style (reanimated/apply-animations-to-style
                                 {:width size
                                  :height size}
                                 {:width size
                                  :height size
                                  :justify-content :space-between})}
        [rn/view {:flex-direction :row :justify-content :space-between}
         [corner :border-top-width :border-left-width]
         [corner :border-top-width :border-right-width]]
        [rn/view {:flex-direction :row :justify-content :space-between}
         [corner :border-bottom-width :border-left-width]
         [corner :border-bottom-width :border-right-width]]]]])])

(defn preview
  []
  [:f>
   (fn []
     (let [initial-dimensions (reanimated/use-shared-value 150)
           _ (react/effect! (fn []
                              (reanimated/animate-shared-value-with-delay initial-dimensions 350 200 :easing2 300) []))]
       [:<>
        [rn/image-background {:style {:width "100%"
                                      :height "100%"
                                      :background-color "red"}}
         [rn/view {:style {:justify-content :center
                           :align-items :center
                           :height "100%"}}
          [viewfinder initial-dimensions]]]]))])