(ns status-im2.common.floating-button-page.floating-container.style
  (:require  [quo.foundations.colors :as colors]
             [react-native.platform :as platform]))

;; TODO: consider modals and customizable props
(defn container
  [{:keys [top]} button-height theme background-shown? blur?]
  {:width            "100%"
   :padding-left     20
   :padding-right    20
   :padding-top      12
   :padding-bottom   12
   :align-self       :flex-end
   ;;    :margin-bottom (+ top 10)
   :background-color (when (and background-shown? (not blur?))
                       (colors/theme-colors colors/black colors/neutral-70 theme))})


(defn view-container
  [keyboard-shown? insets button-height theme background-shown?]
  (container insets button-height theme background-shown? false))

(defn blur-container
  [_ insets button-height theme background-shown?]
  (merge (container insets button-height theme background-shown? true)
         (when platform/android? {:padding-bottom 12})))
