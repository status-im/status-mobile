(ns quo.components.tags.context-tag.style
  (:require
    [quo.foundations.colors :as colors]))

(defn context-tag-icon-color
  [theme blur?]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-40 theme)
    (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)))

(defn audio-tag-icon-container
  [customization-color theme]
  {:width            20
   :height           20
   :border-radius    10
   :align-items      :center
   :justify-content  :center
   :background-color (colors/theme-colors
                      (colors/custom-color customization-color 50)
                      (colors/custom-color customization-color 60)
                      theme)})

(def audio-tag-icon-color colors/white)

(defn container
  [{:keys [theme type size state blur? customization-color]}]
  (let [background-color (if blur?
                           (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-5 theme)
                           (colors/theme-colors colors/neutral-10 colors/neutral-90 theme))
        border-radius    (cond
                           (not (#{:account :collectible} type)) 16
                           (= size 24)                           8
                           :else                                 10)
        border-color     (colors/theme-colors
                          (colors/custom-color customization-color 50)
                          (colors/custom-color customization-color 60)
                          theme)]
    (cond-> {:padding          2
             :padding-right    8
             :flex-direction   :row
             :align-items      :center
             :height           size
             :background-color background-color
             :border-radius    border-radius
             :flex-shrink      1}
      (= state :selected) (assoc :height       (+ size 2)
                                 :border-color border-color
                                 :border-width 1))))

(defn tag-container
  [size]
  {:margin-right   (if (= size 24) 6 10)
   :flex-direction :row
   :flex-shrink    1
   :align-items    :center})

(defn tag-spacing
  [size shrinkable?]
  (cond-> {:margin-left (if (= size 24) 4 8)}
    shrinkable? (assoc :flex-shrink 1)))

(defn text
  ([theme]
   (text theme false))
  ([theme gray-text?]
   {:color (if gray-text?
             colors/neutral-50
             (colors/theme-colors colors/neutral-100 colors/white theme))}))

(defn token-logo
  [size]
  {:border-radius (if (= size 24) 10 14)})

(defn circle-logo
  [size]
  (if (= size 24)
    {:width 20 :height 20 :border-radius 10}
    {:width 28 :height 28 :border-radius 14}))

(defn rounded-logo
  [size]
  (if (= size 24)
    {:width         20
     :height        20
     :border-radius 6}
    {:width         28
     :height        28
     :border-radius 8}))

(defn address
  [size]
  (if (= size 24)
    {:margin-horizontal 6
     :margin-vertical   1
     :flex-direction    :row
     :align-items       :center}
    {:margin-horizontal 10
     :margin-vertical   3
     :flex-direction    :row
     :align-items       :center}))

(defn icon
  [size]
  (if (= size 24)
    {:margin-horizontal 6
     :margin-vertical   1
     :flex-direction    :row
     :align-items       :center}
    {:margin-left     8
     :margin-right    10
     :margin-vertical 3
     :flex-direction  :row
     :align-items     :center}))

(defn icon-spacing
  [size]
  (if (= size 24)
    {:margin-left 4}
    {:margin-left 2}))
