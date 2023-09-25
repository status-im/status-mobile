(ns quo2.components.inputs.address-input.style
  (:require [quo2.foundations.colors :as colors]
            [quo2.components.markdown.text :as text]
            [react-native.platform :as platform]))

(def container
  {:padding-horizontal 20
   :padding-top        8
   :padding-bottom     16
   :height             48
   :flex-direction     :row
   :align-items        :flex-start})

(def buttons-container
  {:flex-direction :row
   :align-items    :center
   :padding-top    (when platform/android? 2)})

(def clear-icon-container
  {:justify-content :center
   :align-items     :center
   :padding-top     (if platform/ios? 6 2)
   :height          24
   :width           20})

(defn input-text
  [theme]
  (assoc (text/text-style {:size   :paragraph-1
                           :weight :monospace})
         :flex         1
         :color        (colors/theme-colors colors/neutral-100 colors/white theme)
         :margin-top   (if platform/ios? 0 -4)
         :margin-right 8
         :height       (if platform/ios? 24 40)))

(defn accessory-button
  [blur? theme]
  {:border-color (if blur?
                   (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10 theme)
                   (colors/theme-colors colors/neutral-30 colors/neutral-70 theme))})
