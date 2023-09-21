(ns status-im2.contexts.quo-preview.style
  (:require [quo2.foundations.colors :as colors]
            [quo2.foundations.typography :as typography]))

;;;; Form fields

(def field-border-radius 12)
(def field-flex-percentage 0.6)
(def text-default typography/paragraph-1)

(defn field-active-bg-color
  []
  (colors/theme-colors colors/primary-50 colors/primary-60))

(defn field-default-color
  []
  (colors/theme-colors colors/neutral-100 colors/white))

(defn field-default-bg-color
  []
  (colors/theme-colors colors/neutral-20 colors/neutral-80))

(defn field-default-border-color
  []
  (colors/theme-colors colors/neutral-30 colors/neutral-70))

(def field-row
  {:flex-direction   :row
   :padding-vertical 6
   :align-items      :center})

(def field-column
  {:flex field-flex-percentage})

(defn field-container
  [active?]
  (merge text-default
         {:color              (if active?
                                (colors/theme-colors colors/white colors/white-opa-95)
                                (colors/theme-colors colors/neutral-100 colors/white))
          :border-width       1
          :border-color       (field-default-border-color)
          :border-radius      field-border-radius
          :padding-vertical   9
          :padding-horizontal 12}))

(defn field-text
  [active?]
  (merge text-default
         {:color (if active?
                   (colors/theme-colors colors/white colors/white-opa-95)
                   (field-default-color))}))

(def customizer-container
  {:flex-shrink 1
   :padding-top 12})

(defn select-container
  []
  (merge (field-container false)
         {:flex-direction   :row
          :align-items      :center
          :border-radius    field-border-radius
          :background-color (field-default-bg-color)
          :border-width     1
          :border-color     (field-default-border-color)}))

(defn field-select
  []
  (merge text-default
         {:flex-grow 1
          :color     (field-default-color)}))

(defn select-option
  [selected?]
  (merge (field-container selected?)
         {:justify-content :center
          :flex            1
          :margin-vertical 4}
         (if selected?
           {:border-color     (field-active-bg-color)
            :background-color (field-active-bg-color)}
           {:background-color (field-default-bg-color)})))

(defn select-button
  []
  (merge (select-option false) {:align-items :center}))

(def label-container
  {:flex          (- 1 field-flex-percentage)
   :padding-right 8})

(defn label
  []
  (merge text-default
         typography/font-medium
         {:color (field-default-color)}))

(defn boolean-container
  []
  {:flex-direction :row
   :flex           field-flex-percentage
   :border-radius  field-border-radius})

(defn boolean-button
  [{:keys [active? left?]}]
  (cond-> {:flex                1
           :align-items         :center
           :justify-content     :center
           :padding-vertical    9
           :padding-horizontal  12
           :border-color        (if active?
                                  (field-active-bg-color)
                                  (field-default-border-color))
           :border-top-width    1
           :border-bottom-width 1
           :background-color    (if active?
                                  (field-active-bg-color)
                                  (field-default-bg-color))}
    left?
    (assoc :border-top-left-radius    field-border-radius
           :border-bottom-left-radius field-border-radius
           :border-left-width         1)

    (not left?)
    (assoc :border-top-right-radius    field-border-radius
           :border-bottom-right-radius field-border-radius
           :border-right-width         1)))

;;;; Modal

(defn modal-overlay
  []
  {:flex               1
   :justify-content    :center
   :padding-horizontal 24
   :background-color   (colors/theme-colors colors/neutral-80-opa-60 colors/neutral-80-opa-80)})

(defn modal-container
  []
  {:padding-horizontal 16
   :padding-vertical   8
   :border-radius      12
   :margin-vertical    100
   :background-color   (colors/theme-colors colors/white colors/neutral-95)})

(defn footer
  []
  {:flex-direction   :row
   :padding-top      10
   :margin-top       10
   :border-top-width 1
   :border-top-color (colors/theme-colors colors/neutral-10 colors/neutral-80)})

;;;; Misc

(defn panel-basic
  []
  {:background-color (colors/theme-colors colors/white colors/neutral-95)
   :flex             1})

(def component-container
  {:flex-grow          1
   :min-height         200
   :padding-vertical   20
   :padding-horizontal 20})

(defn main
  []
  {:flex               1
   :padding-bottom     8
   :padding-horizontal 16
   :background-color   (colors/theme-colors colors/white colors/neutral-90)})
