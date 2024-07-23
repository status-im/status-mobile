(ns status-im.contexts.preview.quo.style
  (:require
    [quo.foundations.colors :as colors]
    [quo.foundations.typography :as typography]))

;;;; Form fields

(def field-border-radius 12)
(def field-flex-percentage 0.6)
(def text-default typography/paragraph-1)

(defn field-active-bg-color
  [theme]
  (colors/theme-colors colors/primary-50 colors/primary-60 theme))

(defn field-default-color
  [theme]
  (colors/theme-colors colors/neutral-100 colors/white theme))

(defn field-default-bg-color
  [theme]
  (colors/theme-colors colors/neutral-20 colors/neutral-80 theme))

(defn field-default-border-color
  [theme]
  (colors/theme-colors colors/neutral-30 colors/neutral-70 theme))

(def field-row
  {:flex-direction   :row
   :padding-vertical 6
   :align-items      :center})

(def field-column
  {:flex field-flex-percentage})

(defn field-container
  [active? theme]
  (merge text-default
         {:color              (if active?
                                (colors/theme-colors colors/white colors/white-opa-95 theme)
                                (colors/theme-colors colors/neutral-100 colors/white theme))
          :border-width       1
          :border-color       (field-default-border-color theme)
          :border-radius      field-border-radius
          :padding-vertical   9
          :padding-horizontal 12}))

(defn field-text
  [active? theme]
  (merge text-default
         {:color (if active?
                   (colors/theme-colors colors/white colors/white-opa-95 theme)
                   (field-default-color theme))}))

(def customizer-container
  {:flex-shrink 1
   :padding-top 12})

(defn multi-select-option
  [theme]
  (merge (field-container false theme)
         {:justify-content  :space-between
          :align-items      :space-between
          :flex             1
          :flex-direction   :row
          :margin-vertical  4
          :background-color (field-default-bg-color theme)}))

(defn select-container
  [theme]
  (merge (field-container false theme)
         {:flex-direction   :row
          :align-items      :center
          :border-radius    field-border-radius
          :background-color (field-default-bg-color theme)
          :border-width     1
          :border-color     (field-default-border-color theme)}))

(defn field-select
  [theme]
  (merge text-default
         {:flex-grow 1
          :color     (field-default-color theme)}))

(defn select-option
  [selected? theme]
  (merge (field-container selected? theme)
         {:justify-content :center
          :flex            1
          :margin-vertical 4}
         (if selected?
           {:border-color     (field-active-bg-color theme)
            :background-color (field-active-bg-color theme)}
           {:background-color (field-default-bg-color theme)})))

(defn select-button
  [theme]
  (merge (select-option false theme) {:align-items :center}))

(def label-container
  {:flex          (- 1 field-flex-percentage)
   :padding-right 8})

(defn label
  [theme]
  (merge text-default
         typography/font-medium
         {:color (field-default-color theme)}))

(defn boolean-container
  []
  {:flex-direction :row
   :flex           field-flex-percentage
   :border-radius  field-border-radius})

(defn boolean-button
  [{:keys [active? left?]} theme]
  (cond-> {:flex                1
           :align-items         :center
           :justify-content     :center
           :padding-vertical    9
           :padding-horizontal  12
           :border-color        (if active?
                                  (field-active-bg-color theme)
                                  (field-default-border-color theme))
           :border-top-width    1
           :border-bottom-width 1
           :background-color    (if active?
                                  (field-active-bg-color theme)
                                  (field-default-bg-color theme))}
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
  [theme]
  {:flex               1
   :justify-content    :center
   :padding-horizontal 24
   :background-color   (colors/theme-colors colors/neutral-80-opa-60 colors/neutral-80-opa-80 theme)})

(defn modal-container
  [theme]
  {:padding-horizontal 16
   :padding-vertical   8
   :border-radius      12
   :margin-vertical    100
   :background-color   (colors/theme-colors colors/white colors/neutral-95 theme)})

(defn footer
  [theme]
  {:flex-direction   :row
   :padding-top      10
   :margin-top       10
   :border-top-width 1
   :border-top-color (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)})

;;;; Misc

(defn panel-basic
  [theme]
  {:background-color (colors/theme-colors colors/white colors/neutral-95 theme)
   :flex             1})

(def component-container
  {:flex-grow          1
   :min-height         200
   :padding-vertical   20
   :padding-horizontal 20})

(defn main
  [theme]
  {:flex               1
   :padding-bottom     8
   :padding-horizontal 16
   :background-color   (colors/theme-colors colors/white colors/neutral-90 theme)})
