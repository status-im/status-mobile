(ns quo2.components.loaders.skeleton
  (:require [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.masked-view :as masked-view]
            [react-native.reanimated :as ra]
            [reagent.core :as reagent]))

(def message-skeleton-height 54)

(def avatar-skeleton-size 32)

(def message-content-width
  [{:author  80
    :message 249}
   {:author  124
    :message 156}
   {:author  96
    :message 212}
   {:author  112
    :message 144}])

;; Standlone message skeleton
(defn message-skeleton
  []
  [:f>
   (fn []
     (let [color                   (colors/theme-colors colors/neutral-5 colors/neutral-70)
           loading-color           (colors/theme-colors colors/neutral-10 colors/neutral-60)
           content-width           (rand-nth message-content-width)
           author-width            (content-width :author)
           message-width           (content-width :message)
           {window-width :width}   (rn/use-window-dimensions)
           translate-x             (ra/use-val (- window-width))
           animated-gradient-style (ra/apply-animations-to-style
                                    {:transform [{:translateX translate-x}]}
                                    {:width  window-width
                                     :height "100%"})]
       (ra/animate-repeat translate-x window-width (- 1) false 1000 :linear)
       [masked-view/masked-view
        {:style       {:height message-skeleton-height}
         :maskElement (reagent/as-element
                       [rn/view
                        {:style {:height           message-skeleton-height
                                 :flex-direction   :row
                                 :padding-vertical 11
                                 :background-color :transparent
                                 :padding-left     21}}
                        [rn/view
                         {:style {:height           avatar-skeleton-size
                                  :width            avatar-skeleton-size
                                  :border-radius    (/ avatar-skeleton-size 2)
                                  :background-color color
                                  :overflow         :hidden}}]
                        [rn/view
                         {:style {:padding-left     8
                                  :background-color :transparent}}
                         [rn/view
                          {:style {:height           8
                                   :width            author-width
                                   :border-radius    6
                                   :background-color color
                                   :margin-bottom    8
                                   :overflow         :hidden}}]
                         [rn/view
                          {:style {:height           16
                                   :width            message-width
                                   :border-radius    6
                                   :overflow         :hidden
                                   :background-color color}}]]])}
        [rn/view
         {:style {:flex             1
                  :background-color color}}
         [ra/linear-gradient
          {:colors [color color loading-color color color]
           :start  {:x 0 :y 0}
           :end    {:x 1 :y 0}
           :style  animated-gradient-style}]]]))])

(defn skeleton
  [parent-height]
  (let [number-of-skeletons (int (Math/floor (/ parent-height message-skeleton-height)))]
    [rn/view
     {:style {:background-color (colors/theme-colors
                                 colors/white
                                 colors/neutral-90)
              :flex             1}}
     (for [n (range number-of-skeletons)]
       [message-skeleton {:key n}])]))
