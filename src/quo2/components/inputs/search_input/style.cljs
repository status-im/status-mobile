(ns quo2.components.inputs.search-input.style
  (:require [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]))

(defn placeholder-color
  [state blur? override-theme]
  (cond
    (and blur? (= state :active))
    (colors/theme-colors colors/neutral-80-opa-20 colors/white-opa-20 override-theme)

    blur? ; state is default
    (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-30 override-theme)

    (= state :active)
    (colors/theme-colors colors/neutral-30 colors/neutral-60 override-theme)

    :else ; Not blur and state is default
    (colors/theme-colors colors/neutral-40 colors/neutral-50 override-theme)))

(def clear-icon-container
  {:justify-content :center
   :align-items     :center
   :margin-left     8
   :height          20
   :width           20})

(defn clear-icon
  [blur? override-theme]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-30 colors/white-opa-10 override-theme)
    (colors/theme-colors colors/neutral-40 colors/neutral-50 override-theme)))

(defn cursor
  [customization-color override-theme]
  (colors/theme-colors (colors/custom-color customization-color 50)
                       (colors/custom-color customization-color 60)
                       override-theme))

(def tag-separator {:width 8})

(def tag-container
  {:flex-direction :row
   :margin-right   8})

(def container
  {:flex           1
   :flex-direction :row
   :align-items    :center})

(def scroll-container
  {:flex-direction :row
   :flex           1
   :height         32})

(def scroll-content
  {:flex-grow       1
   :align-items     :center
   :justify-content :flex-start})

(defn input-text
  [disabled?]
  (assoc (text/text-style {:size   :paragraph-1
                           :weight :regular})
         :flex             1
         :padding-vertical 5
         :min-width        120
         :opacity          (if disabled? 0.3 1)
         :min-height       32))
