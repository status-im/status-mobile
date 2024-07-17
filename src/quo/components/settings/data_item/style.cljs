(ns quo.components.settings.data-item.style
  (:require
    [quo.foundations.colors :as colors]))

(defn container
  [{:keys [size card? blur? actionable? theme]}]
  {:flex-direction     :row
   :justify-content    :space-between
   :padding-vertical   (case size
                         :default 8
                         :large   12
                         nil)
   :padding-horizontal (case size
                         :default 12
                         :large   16
                         nil)
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
  {:width            (case size
                       :large 132
                       :small 72
                       132)
   :height           (case size
                       :large 16
                       :small 10
                       16)
   :background-color (if blur?
                       colors/white-opa-5
                       (colors/theme-colors colors/neutral-5
                                            colors/neutral-90
                                            theme))
   :border-radius    (case size
                       :large 6
                       :small 3
                       6)
   :margin-vertical  (case size
                       :large 4
                       :small 3
                       4)})

(def subtitle-container
  {:flex-direction :row
   :align-items    :center
   :margin-bottom  1})

(def right-container
  {:flex-direction :row
   :align-items    :center})

(defn subtitle-icon-container
  [subtitle-type]
  {:margin-right    (when-not (contains? #{:editable :default} subtitle-type) 4)
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
(def right-icon
  {:margin-left 12})

(def left-side
  {:flex 1})
