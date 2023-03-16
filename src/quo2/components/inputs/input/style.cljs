(ns quo2.components.inputs.input.style
  (:require [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]))

(def variants-colors
  "Colors that keep the same across input's status change"
  {:light      {:label         colors/neutral-50
                :icon          colors/neutral-50
                :cursor        (colors/custom-color :blue 50)
                :button-border colors/neutral-30
                :clear-icon    colors/neutral-40
                :password-icon colors/neutral-50}
   :light-blur {:label         colors/neutral-80-opa-40
                :icon          colors/neutral-80-opa-70
                :cursor        (colors/custom-color :blue 50)
                :button-border colors/neutral-80-opa-30
                :password-icon colors/neutral-100
                :clear-icon    colors/neutral-80-opa-30}
   :dark       {:label         colors/neutral-40
                :icon          colors/neutral-40
                :cursor        (colors/custom-color :blue 60)
                :button-border colors/neutral-70
                :password-icon colors/white
                :clear-icon    colors/neutral-60}
   :dark-blur  {:label         colors/white-opa-40
                :icon          colors/white-opa-70
                :cursor        colors/white
                :button-border colors/white-opa-10
                :password-icon colors/white
                :clear-icon    colors/white-opa-10}})

(def status-colors
  {:light      {:default  {:border-color colors/neutral-20
                           :placeholder  colors/neutral-40
                           :text         colors/neutral-100}
                :focus    {:border-color colors/neutral-40
                           :placeholder  colors/neutral-30
                           :text         colors/neutral-100}
                :error    {:border-color colors/danger-opa-40
                           :placeholder  colors/neutral-40
                           :text         colors/neutral-100}
                :disabled {:border-color colors/neutral-20
                           :placeholder  colors/neutral-40
                           :text         colors/neutral-40}}
   :light-blur {:default  {:border-color colors/neutral-80-opa-10
                           :placeholder  colors/neutral-80-opa-40
                           :text         colors/neutral-100}
                :focus    {:border-color colors/neutral-80-opa-20
                           :placeholder  colors/neutral-80-opa-20
                           :text         colors/neutral-100}
                :error    {:border-color colors/danger-opa-40
                           :placeholder  colors/neutral-80-opa-40
                           :text         colors/neutral-100}
                :disabled {:border-color colors/neutral-80-opa-10
                           :placeholder  colors/neutral-80-opa-30
                           :text         colors/neutral-80-opa-30}}
   :dark       {:default  {:border-color colors/neutral-80
                           :placeholder  colors/neutral-50
                           :text         colors/white}
                :focus    {:border-color colors/neutral-60
                           :placeholder  colors/neutral-60
                           :text         colors/white}
                :error    {:border-color colors/danger-opa-40
                           :placeholder  colors/white-opa-40
                           :text         colors/white}
                :disabled {:border-color colors/neutral-80
                           :placeholder  colors/neutral-40
                           :text         colors/neutral-40}}
   :dark-blur  {:default  {:border-color colors/white-opa-10
                           :placeholder  colors/white-opa-40
                           :text         colors/white}
                :focus    {:border-color colors/white-opa-40
                           :placeholder  colors/white-opa-20
                           :text         colors/white}
                :error    {:border-color colors/danger-opa-40
                           :placeholder  colors/white-opa-40
                           :text         colors/white}
                :disabled {:border-color colors/white-opa-10
                           :placeholder  colors/white-opa-20
                           :text         colors/white-opa-20}}})

(defn input-container
  [colors-by-status small? disabled?]
  {:flex-direction     :row
   :padding-horizontal 8
   :border-width       1
   :border-color       (:border-color colors-by-status)
   :border-radius      (if small? 10 14)
   :opacity            (if disabled? 0.3 1)})

(defn left-icon-container
  [small?]
  {:margin-left  (if small? 0 4)
   :margin-right (if small? 4 8)
   :margin-top   (if small? 5 9)
   :height       20
   :width        20})

(defn icon
  [colors-by-variant]
  {:color (:icon colors-by-variant)
   :size  20})

(defn input
  [colors-by-status small? multiple-lines?]
  (merge (text/text-style {:size :paragraph-1 :weight :regular})
         {:flex                1
          :text-align-vertical :top
          :padding-horizontal  0
          :padding-vertical    (if small? 4 8)
          :color               (:text colors-by-status)}
         (when-not multiple-lines?
           {:height (if small? 30 38)})))

(defn right-icon-touchable-area
  [small?]
  {:margin-left   (if small? 4 8)
   :padding-right (if small? 0 4)
   :padding-top   (if small? 5 9)})

(defn password-icon
  [variant-colors]
  {:size  20
   :color (:password-icon variant-colors)})

(defn clear-icon
  [variant-colors]
  {:size  20
   :color (:clear-icon variant-colors)})

(def texts-container
  {:flex           1
   :flex-direction :row
   :height         18
   :margin-bottom  8})

(def label-container {:flex 1})

(defn label-color
  [variant-colors]
  {:color (:label variant-colors)})

(def counter-container
  {:flex        1
   :align-items :flex-end})

(defn counter-color
  [current-chars char-limit variant-colors]
  {:color (if (> current-chars char-limit)
            colors/danger-60
            (:label variant-colors))})

(defn button
  [colors-by-variant small?]
  {:justify-content    :center
   :align-items        :center
   :height             24
   :border-width       1
   :border-color       (:button-border colors-by-variant)
   :border-radius      8
   :margin-vertical    (if small? 3 7)
   :margin-left        4
   :margin-right       (if small? -4 0)
   :padding-horizontal 7
   :padding-top        1.5
   :padding-bottom     2.5})

(defn button-text
  [colors-by-status]
  {:color (:text colors-by-status)})
