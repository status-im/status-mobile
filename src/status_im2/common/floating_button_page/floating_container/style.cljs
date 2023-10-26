(ns status-im2.common.floating-button-page.floating-container.style
  (:require [react-native.platform :as platform]))

(def container
  {:width      "100%"
   :margin-top :auto
   :align-self :flex-end})

(def view-container
  (assoc container
         :padding-left   20
         :padding-right  20
         :padding-top    12
         :padding-bottom 12))

(def blur-container
  (merge container
         (when platform/android? {:margin-left 20})))
