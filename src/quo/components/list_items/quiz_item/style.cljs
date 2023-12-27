(ns quo.components.list-items.quiz-item.style
  (:require [quo.foundations.colors :as colors]))

(defn container
  [{:keys [blur? theme state]}]
  {:flex             1
   :flex-direction   :row
   :justify-content  :space-between
   :align-items      :center
   :max-height       56
   :padding          12
   :border-radius    12
   :opacity          (if (= state :disabled) 0.3 1)
   :border-width     (if (and blur? (or (= state :empty) (= state :disabled))) 0 1)
   :border-color     (case state
                       :success colors/success-50-opa-20
                       :error   colors/danger-50-opa-20
                       (colors/theme-colors colors/neutral-20 colors/neutral-80 theme))
   :background-color (case state
                       :empty    (if blur?
                                   colors/white-opa-5
                                   (colors/theme-colors colors/white colors/neutral-80-opa-40 theme))
                       :disabled (if blur?
                                   colors/white-opa-5
                                   (colors/theme-colors colors/neutral-5 colors/neutral-80-opa-40 theme))
                       :success  (colors/theme-colors colors/success-50-opa-10
                                                      colors/success-60-opa-10
                                                      theme)
                       :error    (colors/theme-colors colors/danger-50-opa-10
                                                      colors/danger-60-opa-10
                                                      theme))})

(defn num-container
  [{:keys [blur? theme]}]
  {:width           32
   :height          32
   :justify-content :center
   :align-items     :center
   :border-radius   10
   :border-width    1
   :border-color    (if blur?
                      colors/white-opa-10
                      (colors/theme-colors colors/neutral-20 colors/neutral-70 theme))})

(defn text
  [{:keys [blur? theme state]}]
  {:color (case state
            :success (colors/theme-colors colors/success-50 colors/success-60 theme)
            :error   (colors/theme-colors colors/danger-50 colors/danger-60 theme))})
