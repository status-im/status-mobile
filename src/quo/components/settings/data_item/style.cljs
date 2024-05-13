(ns quo.components.settings.data-item.style
  (:require
    [quo.foundations.colors :as colors]))

(defn container
  [{:keys [size card? blur? actionable? theme]}]
  {:flex-direction     :row
   :justify-content    :space-between
   :padding-vertical   (when (= size :default) 8)
   :padding-horizontal (when (= size :default) 12)
   :border-radius      16
   :border-width       (when (and card? (not= size :small)) 1)
   :background-color   (if blur?
                         (if actionable? :transparent colors/white-opa-5)
                         (colors/theme-colors
                          (if actionable? colors/white colors/neutral-2_5)
                          (if actionable? colors/neutral-95 colors/neutral-90)
                          theme))
   :border-color       (if blur?
                         colors/white-opa-10
                         (colors/theme-colors colors/neutral-10
                                              colors/neutral-80
                                              theme))})

(defn loading-container
  [size blur? theme]
  {:width            (if (= size :default) 132 72)
   :height           (if (= size :default) 16 10)
   :background-color (if blur?
                       colors/white-opa-5
                       (colors/theme-colors colors/neutral-5
                                            colors/neutral-90
                                            theme))
   :border-radius    (if (= size :default) 6 3)
   :margin-vertical  (if (= size :default) 4 3)})

(def subtitle-container
  {:flex-direction :row
   :margin-bottom  1})

(def right-container
  {:flex-direction :row
   :align-items    :center})

(defn subtitle-icon-container
  [subtitle-type]
  {:margin-right    (when (not= :default subtitle-type) 4)
   :justify-content :center})

(defn title
  [blur? theme]
  {:color        (if blur?
                   colors/white-opa-40
                   (colors/theme-colors colors/neutral-50
                                        colors/neutral-40
                                        theme))
   :margin-right 4})

(def title-container
  {:flex-direction :row
   :align-items    :center})

(def image
  {:width  16
   :height 16})

(defn description
  [blur? theme]
  {:color (if blur?
            colors/white
            (colors/theme-colors colors/neutral-100
                                 colors/white
                                 theme))})
(defn right-icon
  [label]
  {:margin-left (if (or (= label :graph) (= label :none)) 12 8)})

(def left-side
  {:flex 1})
