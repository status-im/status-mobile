(ns quo.components.drawers.bottom-actions.style
  (:require
    [quo.foundations.colors :as colors]))

(defn- text-color
  [scroll? blur? theme]
  (cond
    scroll? (if blur?
              colors/white-70-blur
              (colors/theme-colors colors/neutral-80-opa-70
                                   colors/white-opa-70
                                   theme))
    blur?   colors/white-opa-40
    :else   (colors/theme-colors colors/neutral-50
                                 colors/neutral-40
                                 theme)))

(defn container
  [scroll? blur? theme]
  {:background-color (when-not (or scroll? blur?)
                       (colors/theme-colors colors/white colors/neutral-95 theme))})

(defn buttons-container
  [actions]
  {:flex-direction     (if (= actions :two-vertical-actions) :column :row)
   :justify-content    :space-around
   :padding-vertical   12
   :gap                12
   :padding-horizontal 20})

(def button-container
  {:flex 1})

(def description-top
  {:flex-direction  :row
   :align-items     :center
   :gap             5
   :padding-top     12
   :padding-bottom  4
   :justify-content :center})

(defn description-top-text
  [scroll? blur? theme]
  {:color (text-color scroll? blur? theme)})

(defn description-bottom
  [scroll? blur? theme]
  {:color              (text-color scroll? blur? theme)
   :text-align         :center
   :padding-bottom     12
   :padding-horizontal 40})

(def error-message
  {:flex-direction     :row
   :gap                4
   :justify-content    :center
   :align-items        :center
   :padding-top        15
   :padding-horizontal 20
   :padding-bottom     7})
