(ns quo.components.tags.collectible-tag.style
  (:require
    [quo.foundations.colors :as colors]))

(defn container
  [size options blur? theme]
  (let [hold? (= options :hold)]
    (merge {:background-color (if blur?
                                (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-5 theme)
                                (colors/theme-colors colors/neutral-10 colors/neutral-90 theme))
            :flex-direction   :row
            :align-items      :center
            :padding-left     2
            :border-width     (if hold? 1 0)
            :border-radius    0
            :border-color     (colors/resolve-color :success theme)}
           (condp = size
             :size-24 {:height        (if hold? 26 24)
                       :padding-right 10
                       :border-radius 8}
             :size-32 {:height        (if hold? 34 32)
                       :padding-right 12
                       :border-radius 10}))))

(defn options-icon
  [size]
  (assoc
   (condp = size
     :size-24 {:right -8
               :top   -8}
     :size-32 {:right -6
               :top   -6})
   :position
   :absolute))

(defn collectible-img
  [size]
  (condp = size
    :size-24 {:width         20
              :height        20
              :margin-right  6
              :border-radius 6}
    :size-32 {:width         28
              :height        28
              :margin-right  8
              :border-radius 8}))

(defn label
  [theme]
  {:color (colors/theme-colors colors/neutral-100 colors/white theme)})
