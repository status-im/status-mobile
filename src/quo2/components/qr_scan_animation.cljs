(ns quo2.components.qr-scan-animation 
    (:require [quo.design-system.colors :as colors]
              [quo.react :as react]
              [quo.react-native :as rn]
              [quo2.reanimated :as reanimated]
              [status-im.switcher.constants :as constants]))

(defn corner [border1 border2 corner]
  [reanimated/view (assoc {:border-color colors/white-persist :width 60 :height 60} border1 2 border2 2 corner 10)])

(def viewfinder-port
  {:position        :absolute
   :left            0
   :top             0
   :bottom          0
   :z-index         100 
   :right           0
   :align-items     :center
   :justify-content :center
   :flex            1})

(defn- viewfinder [size]
  [:f>
   (fn []
     [rn/view {:style viewfinder-port}
      [reanimated/view {:style (reanimated/apply-animations-to-style
                                {:width size
                                 :height size}
                                {:width size
                                 :height size 
                                 :justify-content :space-between})}
       [rn/view {:flex-direction :row :justify-content :space-between}
        [corner :border-top-width :border-left-width :border-top-left-radius]
        [corner :border-top-width :border-right-width :border-top-right-radius]]
       [rn/view {:flex-direction :row :justify-content :space-between}
        [corner :border-bottom-width :border-left-width :border-bottom-left-radius]
        [corner :border-bottom-width :border-right-width :border-bottom-right-radius]]]])])

(defn preview
  []
  [:f>
     (fn []
  (let [initial-dimensions (reanimated/use-shared-value 100)
        _ (react/effect! (fn []
                           (reanimated/animate-shared-value-with-delay initial-dimensions 350 400 :easing2 300) []))] 
       [:<> 
        [rn/image-background {:source (js/require "../resources/images/ui/hardwallet-card.png")
                              :image-style {:opacity 0.7}
                              :style {:width "100%"
                                      :height "100%"}}
         [rn/view {:style {:justify-content :center
                           :z-index 0
                           :align-items :center
                           :height "100%"}}
          [viewfinder initial-dimensions]]]]))])

;; [reanimated/view {:style (reanimated/apply-animations-to-style
;;                           {:width initial-dimensions
;;                            :height initial-dimensions}
;;                           styles)}]
;; (reanimated/animate-shared-value-with-timing
;;  initial-dimensions 300 300 :easing3)