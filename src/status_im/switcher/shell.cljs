(ns status-im.switcher.shell
  (:require [quo.react-native :as rn]
            [status-im.i18n.i18n :as i18n]
            [quo2.foundations.colors :as colors]
            [quo2.components.markdown.text :as text]
            [quo.components.safe-area :as safe-area]
            [quo2.components.navigation.top-nav :as top-nav]))

(defn placeholder []
  [rn/view {:style {:position            :absolute
                    :top                 0
                    :left                0
                    :right               0
                    :bottom              -1
                    :justify-content     :center
                    :align-items         :center
                    :accessibility-label :shell-placeholder-view}}
   [rn/view {:style {:margin-top       12
                     :width            80
                     :height           80
                     :border-radius    16
                     :background-color colors/neutral-90}}]
   [text/text {:size   :heading-2
               :weight :semi-bold
               :style  {:margin-top 20
                        :color      colors/white}}
    (i18n/label :t/shell-placeholder-title)]
   [text/text {:size   :paragraph-1
               :weight :regular
               :align  :center
               :style  {:margin-top 8
                        :color      colors/white}}
    (i18n/label :t/shell-placeholder-subtitle)]])

(defn shell []
  [safe-area/consumer
   (fn [insets]
     [rn/view {:style {:top              0
                       :left             0
                       :right            0
                       :bottom           -1
                       :position         :absolute
                       :background-color colors/neutral-100}}
      [top-nav/top-nav {:type  :shell
                        :style {:margin-top (:top insets)}}]
      [placeholder]
      [rn/scroll-view {:style {:padding-horizontal 20
                               :flex-direction :row}}
       [text/text {:size   :heading-1
                   :weight :semi-bold
                   :style  {:color      colors/white
                            :margin-top 12}}
        (i18n/label :t/jump-to)]]])])

