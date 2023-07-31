(ns quo2.components.wallet.account-card.style
  (:require
    [quo2.foundations.colors :as colors]))

(defn text-color
  [watch-only? theme]
  (if (and watch-only? (= :light theme))
    colors/neutral-100
    colors/white))

(defn card
  [customization-color watch-only? metrics? theme]
  {:width              162
   :height             (if metrics? 88 68)
   :background-color   (if watch-only?
                         (colors/theme-colors colors/neutral-80-opa-5 colors/neutral-95 theme)
                         (colors/theme-colors
                          (colors/custom-color customization-color 50)
                          (colors/custom-color customization-color 60)
                          theme))
   :border-radius      16
   :border-width       1
   :border-color       (if watch-only?
                         (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-5 theme)
                         colors/neutral-80-opa-10)
   :padding-horizontal 12
   :padding-top        6
   :padding-bottom     10})

(def profile-container
  {:margin-bottom  6
   :flex-direction :row})

(def metrics-container
  {:flex-direction :row
   :align-items    :center})

(defn account-name
  [watch-only? theme]
  {:color       (text-color watch-only? theme)
   :margin-left 2})

(def watch-only-container
  {:flex-direction  :row
   :justify-content :space-between
   :align-items     :center
   :flex            1})

(defn account-value
  [watch-only? theme]
  {:color (text-color watch-only? theme)})

(defn metrics
  [watch-only? theme]
  {:color (if (and watch-only? (= :light theme))
            colors/neutral-80-opa-60
            colors/white-opa-70)})

(defn separator
  [watch-only? theme]
  {:width             2
   :height            2
   :border-radius     20
   :background-color  (if (and watch-only? (= :light theme))
                        colors/neutral-80-opa-20
                        colors/white-opa-40)
   :margin-horizontal 4})

(defn add-account-container
  [theme metrics?]
  {:width              161
   :height             (if metrics? 88 68)
   :border-color       (colors/theme-colors colors/neutral-20 colors/white-opa-5 theme)
   :border-width       1
   :border-style       :dashed
   :align-items        :center
   :justify-content    :center
   :border-radius      16
   :padding-vertical   12
   :padding-horizontal 10})

(def emoji
  {:font-size   10
   :line-height 20})

(defn loader-view
  [width height watch-only? theme]
  {:width            width
   :height           height
   :background-color (if (and watch-only? (= :light theme)) colors/neutral-80-opa-5 colors/white-opa-10)
   :border-radius    6})

(def loader-container
  {:flex-direction :row
   :align-items    :center})
