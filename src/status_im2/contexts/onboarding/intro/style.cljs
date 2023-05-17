(ns status-im2.contexts.onboarding.intro.style
  (:require
    [react-native.platform :as platform]
    [quo2.foundations.colors :as colors]))

(def progress-bar-container
  {:background-color :transparent
   :flex-direction   :row
   :margin-vertical  16})

(defn progress-bar-item
  [index position end?]
  {:height       2
   :flex         1
   :border-width 1
   :border-color (if (= index position) colors/white colors/white-opa-10)
   :margin-right (if end? 0 8)})

(def carousel
  {:height             92
   ;; (padding-top) This insets issue needs a consistent implementation across all screens.
   :padding-top        (if platform/android? 0 44)
   :position           :absolute
   :top                0
   :bottom             0
   :left               0
   :right              0
   :z-index            2
   :background-color   :transparent
   :padding-vertical   12
   :padding-horizontal 20})

(def carousel-text
  {:background-color :transparent
   :color            colors/white})

(def page-container
  {:flex            1
   :justify-content :flex-end})

(def page-image
  {:position     :absolute
   :top          0
   :bottom       0
   :left         0
   :right        0
   :width        "100%"
   :aspect-ratio 1})

(def text-container
  {:flex      1
   :flex-wrap :wrap})

(def plain-text
  {:font-size   13
   :line-height 18
   :font-weight :normal
   :color       (colors/alpha colors/white 0.7)})

(def highlighted-text
  {:flex  1
   :color colors/white})
