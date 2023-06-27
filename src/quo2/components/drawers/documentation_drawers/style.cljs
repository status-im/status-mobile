(ns quo2.components.drawers.documentation-drawers.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.safe-area :as safe-area]))

(def outer-container
  ;; Margin negative to fill the bottom-sheet container where this component is used
  {:margin-bottom (- (+ (safe-area/get-bottom) 8))})

(def container
  {:align-items        :flex-start
   :padding-horizontal 20})

(def content
  {:margin-top    8
   :margin-bottom (+ (safe-area/get-bottom) 8)})

(defn title
  [shell?]
  {:color (colors/theme-colors colors/neutral-100
                               colors/white
                               (when shell? :dark))})
