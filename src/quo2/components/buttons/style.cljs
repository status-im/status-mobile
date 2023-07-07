(ns quo2.components.buttons.style)

(def blur-view
  {:position :absolute
   :top      0
   :left     0
   :right    0
   :bottom   0})

(defn before-icon-style
  [{:keys [override-margins size icon-container-size icon-background-color icon-container-rounded?
           icon-size]}]
  (merge
   {:margin-left     (or (get override-margins :left)
                         (if (= size 40) 12 8))
    :margin-right    (or (get override-margins :right) 4)
    :align-items     :center
    :justify-content :center}
   (when icon-container-size
     {:width  icon-container-size
      :height icon-container-size})
   (when icon-background-color
     {:background-color icon-background-color})
   (when icon-container-rounded?
     {:border-radius (/ (or icon-container-size icon-size) 2)})))

(defn after-icon-style
  [{:keys [override-margins size icon-container-size icon-background-color icon-container-rounded?
           icon-size]}]
  (merge
   {:margin-left     (or (get override-margins :left) 4)
    :margin-right    (or (get override-margins :right)
                         (if (= size 40) 12 8))
    :align-items     :center
    :justify-content :center}
   (when icon-container-size
     {:width  icon-container-size
      :height icon-container-size})
   (when icon-background-color
     {:background-color icon-background-color})
   (when icon-container-rounded?
     {:border-radius (/ (or icon-container-size icon-size) 2)})))
