(ns quo.components.inputs.search-input.style
  (:require
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]))

(defn placeholder-color
  [state blur? theme]
  (cond
    (and blur? (= state :active))
    (colors/theme-colors colors/neutral-80-opa-20 colors/white-opa-20 theme)

    blur? ; state is default
    (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-30 theme)

    (= state :active)
    (colors/theme-colors colors/neutral-30 colors/neutral-60 theme)

    :else ; Not blur and state is default
    (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)))

(def clear-icon-container
  {:justify-content :center
   :align-items     :center
   :margin-left     8
   :height          20
   :width           20})

(defn clear-icon
  [blur? theme]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-30 colors/white-opa-10 theme)
    (colors/theme-colors colors/neutral-40 colors/neutral-60 theme)))

(defn cursor
  [customization-color theme]
  (colors/resolve-color customization-color theme))

(def tag-separator {:width 8})

(def tag-container
  {:flex-direction :row
   :margin-right   8})

(defn container
  [container-style]
  (merge {:flex           1
          :flex-direction :row
          :align-items    :center}
         container-style))

(def scroll-container
  {:flex-direction :row
   :flex           1
   :height         32})

(def scroll-content
  {:flex-grow       1
   :align-items     :center
   :justify-content :flex-start})

(defn input-text
  [disabled? theme]
  (assoc (text/text-style {:size   :paragraph-1
                           :weight :regular}
                          theme)
         :flex             1
         :padding-vertical 5
         :min-width        120
         :opacity          (if disabled? 0.3 1)
         :min-height       32))
