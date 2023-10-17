(ns status-im2.common.floating-button-page.style
  (:require [react-native.platform :as platform]))

(def page-container
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0
   :z-index  100})

(def keyboard-view-style
  {:position :absolute
   :left     0
   :right    0
   :top      0
   :bottom   0})

(defn button-container
  [{:keys [top]}]
  {:width         "100%"
   :padding-left  20
   :padding-right 20
   :padding-top   12
   :align-self    :flex-end
   :margin-bottom (+ top 10) ;; this value is needed for the modal page - perhaps dynamically adjusted
                             ;; based on page type
   :height        64})

(defn view-button-container
  [keyboard-shown? insets]
  (merge (button-container insets)
         (if platform/ios?
           {:margin-bottom (if keyboard-shown? 0 34)}
           {:margin-bottom (if keyboard-shown? 12 34)})))

(defn blur-button-container
  [insets]
  (merge (button-container insets)
         (when platform/android? {:padding-bottom 12})))
