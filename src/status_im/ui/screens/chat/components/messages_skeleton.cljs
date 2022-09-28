(ns status-im.ui.screens.chat.components.messages-skeleton
  (:require [status-im.ui.components.react :as react]
            [quo.react-native :as rn]
            [status-im.ui.components.animation :as animation]
            [reagent.core :as reagent]
            [quo2.foundations.colors :as colors])
  (:require-macros [status-im.utils.views :as views]))

(def message-skeleton-height 54)

(def avatar-skeleton-size 32)

(def message-content-width [{:author 80
                             :message 249}
                            {:author 124
                             :message 156}
                            {:author 96
                             :message 212}
                            {:author 112
                             :message 144}])

(defn animated-gradient-style [animation-value width]
  {:position         :absolute
   :width            width
   :height           "100%"
   :transform        [{:translateX
                       (animation/interpolate
                        animation-value
                        {:inputRange  [0 1]
                         :outputRange [(- width) width]})}]})

;; Standlone message skeleton
(views/defview message-skeleton []
  (views/letsubs [color (colors/theme-colors colors/neutral-5 colors/neutral-70)
                  loading-color (colors/theme-colors colors/neutral-10 colors/neutral-60)
                  content-width (rand-nth message-content-width)
                  author-width (content-width :author)
                  message-width (content-width :message)
                  window-width [:dimensions/window-width]
                  pulse-animation (animation/create-value 0)]
    {:component-did-mount (fn [_]
                            (animation/start
                             (animation/anim-loop
                              (animation/timing
                               pulse-animation
                               {:value 1
                                :duration 1300
                                :easing          (.-linear ^js animation/easing)
                                :useNativeDriver true}))))}

    [react/masked-view
     {:style {:height message-skeleton-height}
      :maskElement (reagent/as-element [rn/view {:style {:height message-skeleton-height
                                                         :flex-direction :row
                                                         :padding-vertical 11
                                                         :background-color :transparent
                                                         :padding-left 21}}
                                        [rn/view {:style {:height avatar-skeleton-size
                                                          :width avatar-skeleton-size
                                                          :border-radius (/ avatar-skeleton-size 2)
                                                          :background-color color
                                                          :overflow :hidden}}]
                                        [rn/view {:style {:padding-left 8 :background-color :transparent}}
                                         [rn/view {:style {:height 8
                                                           :width author-width
                                                           :border-radius 6
                                                           :background-color color
                                                           :margin-bottom 8
                                                           :overflow :hidden}}]
                                         [rn/view {:style {:height 16
                                                           :width message-width
                                                           :border-radius 6
                                                           :overflow :hidden
                                                           :background-color color}}]]])}
     [rn/view {:style {:flex 1 :background-color color}}
      [react/animated-linear-gradient {:colors [color color loading-color color color]
                                       :start {:x 0 :y 0}
                                       :end {:x 1 :y 0}
                                       :style (animated-gradient-style pulse-animation window-width)}]]]))

(def number-of-skeletons (reagent/atom nil))

(defn messages-skeleton []
  [rn/view {:style {:background-color (colors/theme-colors
                                       colors/white
                                       colors/neutral-90)
                    :flex 1}
            :on-layout (fn [^js ev]
                         (let [height (-> ev .-nativeEvent .-layout .-height)
                               skeletons (int (Math/floor (/ height message-skeleton-height)))]
                           (reset! number-of-skeletons skeletons)))}
   (for [n (range @number-of-skeletons)]
     [message-skeleton {:key n}])])