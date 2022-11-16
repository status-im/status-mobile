(ns status-im2.contexts.shell.view
  (:require [i18n.i18n :as i18n]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [status-im2.contexts.shell.constants :as constants]
            [status-im2.contexts.shell.animation :as animation]
            [status-im2.contexts.shell.home-stack :as home-stack]
            [status-im2.contexts.shell.bottom-tabs :as bottom-tabs]))

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
   [quo/text {:size   :heading-2
              :weight :semi-bold
              :style  {:margin-top 20
                       :color      colors/white}}
    (i18n/label :t/shell-placeholder-title)]
   [quo/text {:size   :paragraph-1
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
      [quo/top-nav {:type  :shell
                    :style {:margin-top (:top insets)}}]
      [placeholder]
      [rn/scroll-view {:style {:padding-horizontal 20
                               :flex-direction     :row}}
       [quo/text {:size   :heading-1
                  :weight :semi-bold
                  :style  {:color      colors/white
                           :margin-top 12}}
        (i18n/label :t/jump-to)]]])])

(defn shell-stack []
  [:f>
   (fn []
     (let [shared-values (animation/get-shared-values)]
       [:<>
        [shell]
        [bottom-tabs/bottom-tabs shared-values]
        [home-stack/home-stack shared-values]
        [quo/floating-shell-button
         {:jump-to {:on-press #(animation/close-home-stack shared-values)
                    :label    (i18n/label :t/jump-to)}}
         {:position :absolute
          :bottom   (+ (constants/bottom-tabs-container-height) 7)} ;; bottom offset is 12 = 7 + 5(padding on button)
         (:home-stack-opacity shared-values)
         (:home-stack-pointer shared-values)]]))])
