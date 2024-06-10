(ns quo.components.notifications.notification.style
  (:require
    [quo.foundations.colors :as colors]
    [quo.foundations.shadows :as shadows]
    [react-native.platform :as platform]))

(def box-container
  {:margin-horizontal 8
   :border-radius     16
   :overflow          :hidden})

(def blur-container
  {:height           "100%"
   :width            "100%"
   :position         :absolute
   :padding-vertical 8
   :padding-left     10
   :padding-right    8
   :background-color (when platform/ios? :transparent)})

(defn content-container
  [theme]
  (merge
   (shadows/get 1 theme)
   {:background-color   (colors/theme-colors colors/neutral-80-opa-70 colors/white-opa-70 theme)
    :flex-direction     :row
    :padding-vertical   8
    :padding-horizontal 12}))

(def right-side-container
  {:flex            1
   :justify-content :center})

(defn title
  [theme]
  {:color (colors/theme-colors colors/white colors/neutral-100 theme)})

(defn text
  [theme]
  {:color (colors/theme-colors colors/white colors/neutral-100 theme)})

(defn avatar-container
  [{:keys [multiline?]}]
  {:margin-right 8
   :align-self   (when-not multiline? :center)
   :margin-top   (if multiline? 4 0)})
