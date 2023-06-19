(ns quo2.components.avatars.user-avatar.style
  (:require [quo2.foundations.colors :as colors]))

(def sizes
  {:big    {:dimensions              80
            :status-indicator        20
            :status-indicator-border 4
            :font-size               :heading-1}
   :medium {:dimensions              48
            :status-indicator        12
            :status-indicator-border 2
            :font-size               :heading-2}
   :small  {:dimensions              32
            :status-indicator        12
            :status-indicator-border 2
            :font-size               :paragraph-2}
   :xs     {:dimensions              24
            :status-indicator        0
            :status-indicator-border 0
            :font-size               :paragraph-2}
   :xxs    {:dimensions              20
            :status-indicator        0
            :status-indicator-border 0
            :font-size               :label}
   :xxxs   {:dimensions              16
            :status-indicator        0
            :status-indicator-border 0
            :font-size               :label}})

(def indicator-color
  {:online  colors/success-50
   :offline colors/neutral-40})

(defn outer
  [size]
  (let [dimensions (get-in sizes [size :dimensions])]
    {:width  dimensions
     :height dimensions}))

(defn customization-color
  [color-id theme]
  (colors/custom-color-by-theme color-id 50 60 nil nil theme))

(def initials-avatar-text
  {:color colors/white-opa-70})
