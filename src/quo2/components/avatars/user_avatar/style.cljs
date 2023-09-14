(ns quo2.components.avatars.user-avatar.style
  (:require [quo2.foundations.colors :as colors]))

(def sizes
  {:big    {:dimensions                      80
            :status-indicator                12
            :status-indicator-border         4
            :status-indicator-center-to-edge 12
            :font-size                       :heading-1
            :ring-width                      4}
   64      {:dimensions                      64
            :status-indicator                8
            :status-indicator-border         2
            :status-indicator-center-to-edge 6
            :font-size                       :heading-1
            :ring-width                      2}
   :medium {:dimensions                      48
            :status-indicator                8
            :status-indicator-border         2
            :status-indicator-center-to-edge 6
            :font-size                       :heading-2
            :ring-width                      2}
   :small  {:dimensions                      32
            :status-indicator                8
            :status-indicator-border         2
            :status-indicator-center-to-edge 4
            :font-size                       :paragraph-2
            :ring-width                      2}
   28      {:dimensions                      28
            :status-indicator                0
            :status-indicator-border         0
            :status-indicator-center-to-edge 0
            :font-size                       :paragraph-2
            :ring-width                      0}
   :xs     {:dimensions                      24
            :status-indicator                0
            :status-indicator-border         0
            :status-indicator-center-to-edge 0
            :font-size                       :paragraph-2
            :ring-width                      2}
   :xxs    {:dimensions                      20
            :status-indicator                0
            :status-indicator-border         0
            :status-indicator-center-to-edge 0
            :font-size                       :label
            :ring-width                      2}
   :xxxs   {:dimensions                      16
            :status-indicator                0
            :status-indicator-border         0
            :status-indicator-center-to-edge 0
            :font-size                       :label
            :ring-width                      2}})

(defn initials-avatar
  [size customization-color theme]
  (let [dimensions (get-in sizes [size :dimensions])]
    {:position         :absolute
     :top              0
     :left             0
     :width            dimensions
     :height           dimensions
     :border-radius    dimensions
     :justify-content  :center
     :align-items      :center
     :background-color (colors/custom-color-by-theme customization-color 50 60 nil nil theme)}))

(def indicator-color
  {:online  colors/success-50
   :offline colors/neutral-40})

(defn outer
  [size static-profile-picture?]
  (let [dimensions                     (get-in sizes [size :dimensions])
        outer-style                    {:width  dimensions
                                        :height dimensions}
        outer-style-with-border-radius (assoc outer-style :border-radius dimensions)]
    (if static-profile-picture?
      outer-style-with-border-radius
      outer-style)))

(defn customization-color
  [color-id theme]
  (colors/custom-color-by-theme color-id 50 60 nil nil theme))

(def initials-avatar-text
  {:color colors/white-opa-70})
