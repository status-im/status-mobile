(ns status-im2.common.floating-button-page.floating-container.style
  (:require  [quo.foundations.colors :as colors]
             [react-native.platform :as platform]))

(defn container
  [{:keys [top]} button-height theme background-shown?]
  {:width         "100%"
   :padding-left  20
   :padding-right 20
   :padding-top   12
   :align-self    :flex-end
;;    :margin-bottom (+ top 10)
   :background-color (when background-shown? (colors/theme-colors colors/black colors/neutral-70 theme))
   :height        button-height})

(defn view-container
  [keyboard-shown? insets button-height theme background-shown?]
  (merge (container insets button-height theme background-shown?)
         (if platform/ios?
           {:margin-bottom (if keyboard-shown? 46 46)} ;;figure out
           {:margin-bottom (if keyboard-shown? 12 34)})))

(defn blur-container
  [_ insets button-height theme background-shown?]
  (merge (container insets button-height theme background-shown?)
         (when platform/android? {:padding-bottom 12})))
