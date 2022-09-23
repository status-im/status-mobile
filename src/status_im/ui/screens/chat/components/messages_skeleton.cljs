(ns status-im.ui.screens.chat.components.messages-skeleton
  (:require [quo.react-native :as rn]
            [reagent.core :as reagent]
            [quo2.foundations.colors :as colors]))

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

(defn message-skeleton []
  (let [color (if (colors/dark?) colors/neutral-70 colors/neutral-5)
        content-width (rand-nth message-content-width)
        author-width (content-width :author)
        message-width (content-width :message)]
    [rn/view {:style {:height message-skeleton-height
                      :flex-direction :row
                      :padding-vertical 11
                      :padding-left 21}}
     [rn/view {:style {:height avatar-skeleton-size
                       :width avatar-skeleton-size
                       :border-radius (/ avatar-skeleton-size 2)
                       :background-color color}}]
     [rn/view {:style {:padding-left 8}}
      [rn/view {:style {:height 8
                        :width author-width
                        :border-radius 6
                        :background-color color
                        :margin-bottom 8}}]
      [rn/view {:style {:height 16
                        :width message-width
                        :border-radius 6
                        :background-color color}}]]]))

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