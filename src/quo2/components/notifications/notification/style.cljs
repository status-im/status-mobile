(ns quo2.components.notifications.notification.style
  (:require
    [quo2.foundations.colors :as colors]
    [quo2.foundations.shadows :as shadows]))

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
   :background-color :transparent})

(defn content-container
  [theme]
  (merge
   (shadows/get 1 theme)
   {:background-color   (colors/theme-colors colors/neutral-80-opa-70 colors/white-opa-70 theme)
    :flex-direction     :row
    :padding-vertical   8
    :padding-horizontal 12}))

(def right-side-container {:flex 1})

(defn title
  [override-theme]
  {:color (colors/theme-colors colors/white colors/neutral-100 override-theme)})

(defn text
  [override-theme]
  {:color (colors/theme-colors colors/white colors/neutral-100 override-theme)})

(def avatar-container {:margin-right 8 :margin-top 4})
