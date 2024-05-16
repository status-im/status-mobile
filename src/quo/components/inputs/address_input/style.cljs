(ns quo.components.inputs.address-input.style
  (:require
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]))

(defn container
  [container-style]
  (merge {:padding-horizontal 20
          :padding-top        8
          :padding-bottom     16
          :flex-direction     :row
          :align-items        :flex-start}
         container-style))

(def buttons-container
  {:flex-direction :row
   :align-items    :flex-start})

(def clear-icon-container
  {:justify-content :flex-start
   :align-items     :center
   :padding-top     (when platform/ios? 4)
   :height          24
   :width           20})

(defn input-text
  [theme]
  (assoc (text/text-style {:size   :paragraph-1
                           :weight :monospace}
                          nil)
         :flex         1
         :color        (colors/theme-colors colors/neutral-100 colors/white theme)
         :margin-top   -4
         :margin-right 8
         :max-height   50
         :padding      0))

(defn accessory-button
  [blur? theme]
  {:border-color (if blur?
                   (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10 theme)
                   (colors/theme-colors colors/neutral-30 colors/neutral-70 theme))})
