(ns quo.components.notifications.toast.style
  (:require
    [quo.foundations.colors :as colors]
    [quo.foundations.shadows :as shadows]
    [react-native.platform :as platform]))

(defn box-container
  [theme]
  (merge (shadows/get 1 theme)
         {:margin-horizontal 12
          :border-radius     12
          :overflow          :hidden}))

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
  {:background-color (colors/theme-colors colors/neutral-80-opa-70 colors/white-opa-70 theme)
   :flex-direction   :row
   :justify-content  :space-between
   :padding          8
   :border-radius    12})

(defn title
  [theme]
  {:color (colors/theme-colors colors/white colors/neutral-100 theme)})

(defn text
  [theme]
  {:color (colors/theme-colors colors/white colors/neutral-100 theme)})

(defn icon
  [toast-type theme]
  {:color           (case toast-type
                      :negative (colors/resolve-color :danger theme)
                      :positive (colors/resolve-color :success theme)
                      :neutral  (colors/theme-colors colors/white-opa-40
                                                     colors/neutral-80-opa-40
                                                     theme))
   :container-style {:width 20 :height 20}})

(def left-side-container
  {:padding     2
   :padding-top 3})

(def right-side-container
  {:padding 4
   :flex    1})

(defn action-container
  [theme]
  {:background-color   (colors/theme-colors colors/white-opa-5 colors/neutral-80-opa-5 theme)
   :flex-direction     :row
   :padding-vertical   3
   :padding-horizontal 8
   :align-items        :center
   :border-radius      8})
