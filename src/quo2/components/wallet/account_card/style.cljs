(ns quo2.components.wallet.account-card.style
  (:require
    [quo2.foundations.colors :as colors]))

(defn text-color
  [watch-only?]
  (if (and watch-only? (not (colors/dark?)))
    colors/neutral-100
    colors/white))

(defn card
  [customization-color watch-only?]
  {:width              161
   :height             88
   :background-color   (if watch-only?
                         (colors/theme-colors colors/neutral-80-opa-5 colors/neutral-95)
                         (colors/custom-color-by-theme customization-color 50 60))
   :border-radius      16
   :border-width       1
   :border-color       (if watch-only?
                         (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-5)
                         (colors/custom-color-by-theme customization-color 50 60))
   :padding-horizontal 12
   :padding-top        8
   :padding-bottom     10})

(def profile-container
  {:margin-bottom  8
   :flex-direction :row})

(def metrics-container
  {:flex-direction :row
   :align-items    :center})

(def title
  {:margin-bottom 9})

(defn account-name
  [watch-only?]
  {:style  {:color       (text-color watch-only?)
            :margin-left 5}
   :size   :paragraph-2
   :weight :medium})

(def watch-only-container
  {:flex-direction  :row
   :justify-content :space-between
   :align-items     :center
   :flex            1})

(defn account-value
  [watch-only?]
  {:style  {:color (text-color watch-only?)}
   :size   :heading-2
   :weight :semi-bold})

(defn metrics
  [watch-only?]
  {:weight :semi-bold
   :size   :paragraph-2
   :style  {:color (if (and watch-only? (not (colors/dark?)))
                     colors/neutral-80-opa-60
                     colors/white-opa-70)}})

(defn separator
  [watch-only?]
  {:width             1
   :height            10
   :border-width      1
   :border-color      (if watch-only?
                        colors/neutral-80-opa-20
                        colors/white-opa-40)
   :margin-horizontal 4})

(def add-account-container
  {:width              161
   :height             88
   :border-color       (colors/theme-colors colors/neutral-20 colors/white-opa-5)
   :border-width       1
   :border-style       :dashed
   :align-items        :center
   :justify-content    :center
   :border-radius      16
   :padding-vertical   12
   :padding-horizontal 10})

(def add-account-title
  {:size       :paragraph-2
   :weight     :medium
   :margin-top 4})
