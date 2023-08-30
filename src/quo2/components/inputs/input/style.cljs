(ns quo2.components.inputs.input.style
  (:require [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]))

(defn variants-colors
  [blur? theme]
  (if blur?
    {:label         (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-40 theme)
     :icon          (colors/theme-colors colors/neutral-80-opa-70 colors/white-opa-70 theme)
     :button-border (colors/theme-colors colors/neutral-80-opa-30 colors/white-opa-10 theme)
     :password-icon (colors/theme-colors colors/neutral-100 colors/white-opa-70 theme)
     :clear-icon    (colors/theme-colors colors/neutral-80-opa-30 colors/white-opa-10 theme)
     :cursor        (colors/theme-colors (colors/custom-color :blue 50)
                                         colors/white
                                         theme)}
    {:label         (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
     :icon          (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
     :button-border (colors/theme-colors colors/neutral-30 colors/neutral-70 theme)
     :clear-icon    (colors/theme-colors colors/neutral-40 colors/neutral-60 theme)
     :password-icon (colors/theme-colors colors/neutral-50 colors/white theme)
     :cursor        (colors/theme-colors (colors/custom-color :blue 50)
                                         (colors/custom-color :blue 60)
                                         theme)}))

(defn status-colors
  [status blur? theme]
  (if blur?
    (case status
      :focus
      {:border-color (colors/theme-colors colors/neutral-80-opa-20 colors/white-opa-40 theme)
       :placeholder  (colors/theme-colors colors/neutral-80-opa-20 colors/white-opa-20 theme)
       :text         (colors/theme-colors colors/neutral-100 colors/white theme)}
      :error
      {:border-color (colors/theme-colors colors/danger-opa-40 colors/danger-opa-40 theme)
       :placeholder  (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-40 theme)
       :text         (colors/theme-colors colors/neutral-100 colors/white theme)}
      :disabled
      {:border-color (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10 theme)
       :placeholder  (colors/theme-colors colors/neutral-80-opa-30 colors/white-opa-20 theme)
       :text         (colors/theme-colors colors/neutral-80-opa-30 colors/white-opa-20 theme)}
      ;; :default
      {:border-color (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10 theme)
       :placeholder  (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-40 theme)
       :text         (colors/theme-colors colors/neutral-100 colors/white theme)})
    (case status
      :focus
      {:border-color (colors/theme-colors colors/neutral-40 colors/neutral-60 theme)
       :placeholder  (colors/theme-colors colors/neutral-30 colors/neutral-60 theme)
       :text         (colors/theme-colors colors/neutral-100 colors/white theme)}
      :error
      {:border-color (colors/theme-colors colors/danger-opa-40 colors/danger-opa-40 theme)
       :placeholder  (colors/theme-colors colors/neutral-40 colors/white-opa-40 theme)
       :text         (colors/theme-colors colors/neutral-100 colors/white theme)}
      :disabled
      {:border-color (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
       :placeholder  (colors/theme-colors colors/neutral-40 colors/neutral-40 theme)
       :text         (colors/theme-colors colors/neutral-40 colors/neutral-40 theme)}
      ;; :default
      {:border-color (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
       :placeholder  (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)
       :text         (colors/theme-colors colors/neutral-100 colors/white theme)})))

(defn input-container
  [colors-by-status small? disabled?]
  {:flex-direction     :row
   :padding-horizontal 8
   :border-width       1
   :border-color       (:border-color colors-by-status)
   :border-radius      (if small? 10 12)
   :opacity            (if disabled? 0.3 1)})

(defn left-icon-container
  [small?]
  {:margin-left (if small? 0 4)
   :margin-top  (if small? 5 9)
   :height      20
   :width       20})

(defn icon
  [colors-by-variant]
  {:color (:icon colors-by-variant)
   :size  20})

(defn input
  [colors-by-status small? multiple-lines?]
  (let [base-props (assoc (text/text-style {:size :paragraph-1 :weight :regular})
                          :flex             1
                          :padding-right    0
                          :padding-left     (if small? 4 8)
                          :padding-vertical (if small? 4 8)
                          :color            (:text colors-by-status))]
    (if multiple-lines?
      (assoc base-props :text-align-vertical :top)
      (assoc base-props :height (if small? 30 38) :line-height nil))))

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
  {:flex-direction :row
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
