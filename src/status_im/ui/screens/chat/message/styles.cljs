(ns status-im.ui.screens.chat.message.styles
  (:require [quo.design-system.colors :as colors]
            [status-im.ui.screens.chat.styles.photos :as photos]))

(defn picker-wrapper-style [{:keys [display-photo? outgoing]}]
  (merge {:flex-direction :row
          :flex           1
          :padding-top    4
          :padding-right  8}
         (if outgoing
           {:justify-content :flex-end}
           {:justify-content :flex-start})
         (if display-photo?
           {:padding-left (+ 16 photos/default-size)}
           {:padding-left 8})))

(defn container-style [{:keys [outgoing]}]
  (merge {:border-top-left-radius     16
          :border-top-right-radius    16
          :border-bottom-right-radius 16
          :border-bottom-left-radius  16
          :background-color           (:ui-background @colors/theme)}
         (if outgoing
           {:border-top-right-radius 4}
           {:border-top-left-radius 4})))

(defn reactions-picker-row []
  {:flex-direction     :row
   :padding-vertical   8
   :padding-horizontal 8})

(defn quick-actions-row []
  {:flex-direction   :row
   :justify-content  :space-evenly
   :border-top-width 1
   :border-top-color (:ui-01 @colors/theme)})

(defn reaction-style [{:keys [outgoing own]}]
  (merge {:border-top-left-radius     10
          :border-top-right-radius    10
          :border-bottom-right-radius 10
          :border-bottom-left-radius  10
          :flex-direction             :row
          :margin-vertical            2
          :padding-right              8
          :padding-left               2
          :padding-vertical           2}
         (if own
           {:background-color (:interactive-01 @colors/theme)}
           {:background-color (:interactive-02 @colors/theme)})
         (if outgoing
           {:border-top-right-radius 2
            :margin-left             4}
           {:border-top-left-radius 2
            :margin-right           4})))

(defn reaction-quantity-style [{:keys [own]}]
  {:font-size   12
   :line-height 16
   :color       (if own
                  colors/white
                  (:text-01 @colors/theme))})

(defn reactions-row [{:keys [outgoing display-photo?]}]
  (merge {:flex-direction :row
          :padding-right  8}
         (if outgoing
           {:justify-content :flex-end}
           {:justify-content :flex-start})
         (if display-photo?
           {:padding-left (+ 16 photos/default-size)}
           {:padding-left 8})))

(defn reaction-button [active]
  (merge {:width             40
          :height            40
          :border-radius     20
          :justify-content   :center
          :align-items       :center
          :margin-horizontal 1
          :border-width      1
          :border-color      :transparent}
         (when active
           {:background-color (:interactive-02 @colors/theme)
            ;; FIXME: Use broder color here
            :border-color     "rgba(67, 96, 223, 0.2)"})))
