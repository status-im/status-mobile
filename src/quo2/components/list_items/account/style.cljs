(ns quo2.components.list-items.account.style
  (:require
    [quo2.foundations.colors :as colors]))

(defn- background-color
  [{:keys [state blur? customization-color]}]
  (cond (or (= state :pressed) (= state :selected))
        (if blur? colors/white-opa-5 (colors/custom-color customization-color 50 5))
        (= state :active)
        (if blur? colors/white-opa-10 (colors/custom-color customization-color 50 10))
        (and (= state :pressed) blur?) colors/white-opa-10
        :else :transparent))

(defn container
  [props]
  {:height             56
   :border-radius      12
   :background-color   (background-color props)
   :flex-direction     :row
   :align-items        :center
   :padding-horizontal 12
   :padding-vertical   6
   :justify-content    :space-between})

(def left-container
  {:flex-direction :row
   :align-items    :center})

(defn metric-text
  [type theme]
  {:color (case type
            :balance-positive (colors/theme-colors colors/success-50 colors/success-60 theme)
            :balance-negative (colors/theme-colors colors/danger-50 colors/danger-60 theme)
            (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))})

(defn dot-divider
  [type theme]
  {:width             2
   :height            2
   :border-radius     2
   :margin-horizontal 4
   :background-color  (case type
                        :balance-positive (colors/theme-colors colors/success-50-opa-40
                                                               colors/success-60-opa-40
                                                               theme)
                        :balance-negative (colors/theme-colors colors/danger-50-opa-40
                                                               colors/danger-50-opa-40
                                                               theme)
                        (colors/theme-colors colors/neutral-80-opa-40 colors/neutral-50-opa-40 theme))})

(defn arrow-icon
  [type theme]
  {:size  16
   :color (if (= type :balance-positive)
            (colors/theme-colors colors/success-50 colors/success-60 theme)
            (colors/theme-colors colors/danger-50 colors/danger-60 theme))})

(def arrow-icon-container
  {:margin-left 4})

(def account-container
  {:margin-left 8})

(def account-title-container
  {:flex-direction :row
   :height         22
   :align-items    :center})

(defn account-address
  [blur? theme]
  {:height 18
   :color  (if blur?
             colors/white-opa-40
             (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))})

(def keycard-icon-container
  {:margin-left 4})

(def token-tag-container
  {:height      40
   :padding-top 4})

(defn token-tag-text-container
  [blur? theme]
  {:flex-direction     :row
   :align-items        :center
   :height             16
   :padding-horizontal 3
   :border-width       1
   :border-radius      6
   :border-color       (if blur?
                         colors/white-opa-10
                         (colors/theme-colors colors/neutral-20
                                              colors/neutral-80
                                              theme))})

(defn token-tag-text
  [blur? theme]
  {:margin-top -1
   :color      (if blur?
                 colors/white-opa-70
                 (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))})

(def balance-container
  {:align-items     :flex-end
   :justify-content :space-between})

(def metrics-container
  {:flex-direction :row
   :align-items    :center
   :margin-top     2})
