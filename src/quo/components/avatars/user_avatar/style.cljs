(ns quo.components.avatars.user-avatar.style
  (:require
    [quo.foundations.colors :as colors]))

(def sizes
  {:big     {:dimensions                      80
             :status-indicator                12
             :status-indicator-border         4
             :status-indicator-center-to-edge 12
             :font-size                       :heading-1}
   :size-64 {:dimensions                      64
             :status-indicator                8
             :status-indicator-border         2
             :status-indicator-center-to-edge 6
             :font-size                       :heading-1}
   :medium  {:dimensions                      48
             :status-indicator                8
             :status-indicator-border         2
             :status-indicator-center-to-edge 6
             :font-size                       :heading-2}
   :small   {:dimensions                      32
             :status-indicator                8
             :status-indicator-border         2
             :status-indicator-center-to-edge 4
             :font-size                       :paragraph-2}
   28       {:dimensions                      28
             :status-indicator                0
             :status-indicator-border         0
             :status-indicator-center-to-edge 0
             :font-size                       :paragraph-2}
   :xs      {:dimensions                      24
             :status-indicator                0
             :status-indicator-border         0
             :status-indicator-center-to-edge 0
             :font-size                       :paragraph-2}
   :xxs     {:dimensions                      20
             :status-indicator                0
             :status-indicator-border         0
             :status-indicator-center-to-edge 0
             :font-size                       :label}
   :xxxs    {:dimensions                      16
             :status-indicator                0
             :status-indicator-border         0
             :status-indicator-center-to-edge 0
             :font-size                       :label}})

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
     :background-color (colors/resolve-color customization-color theme)}))

(defn indicator-color
  [theme]
  {:online  (colors/theme-colors colors/success-50 colors/success-60 theme)
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
  (colors/resolve-color color-id theme))

(def initials-avatar-text
  {:color colors/white-opa-70})
