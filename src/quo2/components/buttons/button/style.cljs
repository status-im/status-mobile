(ns quo2.components.buttons.button.style)

(def blur-view
  {:position :absolute
   :top      0
   :left     0
   :right    0
   :bottom   0})

(defn icon-style
  [{:keys [icon-container-size icon-container-rounded?
           icon-size margin-left margin-right]}]
  (cond-> (merge {:margin-left     margin-left
                  :margin-right    margin-right
                  :align-items     :center
                  :justify-content :center})
    icon-container-size
    (assoc :width icon-container-size :height icon-container-size)
    icon-container-rounded?
    (assoc :border-radius (/ (or icon-container-size icon-size) 2))))

(defn icon-left-icon-style
  [{:keys [override-margins size icon-container-size icon-container-rounded?
           icon-size]}]
  (icon-style
   {:margin-left             (or (get override-margins :left)
                                 (if (= size 40) 12 8))
    :margin-right            (or (get override-margins :right) 4)
    :icon-container-size     icon-container-size
    :icon-container-rounded? icon-container-rounded?
    :icon-size               icon-size}))

(defn icon-right-icon-style
  [{:keys [override-margins size icon-container-size icon-container-rounded?
           icon-size]}]
  (icon-style {:margin-left             (or (get override-margins :left) 4)
               :margin-right            (or (get override-margins :right)
                                            (if (= size 40) 12 8))
               :icon-container-size     icon-container-size
               :icon-container-rounded? icon-container-rounded?
               :icon-size               icon-size}))

(defn shape-style-container
  [size border-radius]
  {:height        size
   :border-radius (if border-radius
                    border-radius
                    (case size
                      56 12
                      40 12
                      32 10
                      24 8
                      12))})

(defn style-container
  [{:keys [size disabled? border-radius background-color border-color icon-only? icon-top
           icon-left icon-right]}]
  (merge {:height             size
          :align-items        :center
          :justify-content    :center
          :flex-direction     (if icon-top :column :row)
          :padding-horizontal (when-not (or icon-only? icon-left icon-right)
                                (case size
                                  56 (if border-color 10 11)
                                  40 16
                                  32 12
                                  24 7
                                  16))
          :padding-left       (when-not (or icon-only? icon-left)
                                (case size
                                  56 nil
                                  40 16
                                  32 12
                                  24 8
                                  16))
          :padding-right      (when-not (or icon-only? icon-right)
                                (case size
                                  56 nil
                                  40 16
                                  32 12
                                  24 8
                                  16))
          :padding-top        (when-not (or icon-only? icon-left icon-right)
                                (case size
                                  56 0
                                  40 (if border-color 8 9)
                                  32 (if border-color 4 5)
                                  24 (if border-color 0 1)
                                  (if border-color 8 9)))
          :padding-bottom     (when-not (or icon-only? icon-left icon-right)
                                (case size
                                  56 0
                                  40 9
                                  32 5
                                  24 4
                                  9))
          :overflow           :hidden
          :background-color   background-color
          :border-radius      (if border-radius
                                border-radius
                                (case size
                                  56 12
                                  40 12
                                  32 10
                                  24 8
                                  12))
          :border-color       border-color
          :border-width       (when border-color 1)}
         (when icon-only?
           {:width size})
         (when border-color
           {:border-color border-color
            :border-width 1})
         (when disabled
           {:opacity 0.7})))
