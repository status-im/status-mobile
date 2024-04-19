(ns quo.components.wallet.account-card.style
  (:require
    [quo.foundations.colors :as colors]))

(defn text-color
  [type theme]
  (if (and (or (= :missing-keypair type)
               (= :watch-only type))
           (= :light theme))
    colors/neutral-100
    colors/white))

(defn card
  [{:keys [customization-color type theme pressed? metrics?]}]
  {:width              161
   :height             (if metrics? 88 68)
   :background-color   (when (not= :watch-only type)
                         (colors/theme-colors
                          (colors/resolve-color customization-color
                                                theme
                                                (when (= :missing-keypair type) (if pressed? 20 10)))
                          (colors/resolve-color customization-color
                                                theme
                                                (when (= :missing-keypair type) (if pressed? 30 20)))
                          theme))
   :border-radius      16
   :border-width       1
   :border-color       (if (or (= :missing-keypair type)
                               (= :watch-only type))
                         (colors/theme-colors
                          (if pressed? colors/neutral-80-opa-10 colors/neutral-80-opa-5)
                          (if pressed? colors/white-opa-10 colors/white-opa-5)
                          theme)
                         colors/neutral-80-opa-10)
   :padding-horizontal 12
   :padding-top        6
   :padding-bottom     9})

(def profile-container
  {:margin-bottom  8
   :flex-direction :row})

(def metrics-container
  {:flex-direction :row
   :align-items    :center})

(defn account-name
  [type theme]
  {:color       (text-color type theme)
   :margin-left 2})

(def watch-only-container
  {:flex-direction  :row
   :justify-content :space-between
   :align-items     :center
   :flex            1})

(defn account-value
  [type theme]
  {:color (text-color type theme)})

(defn metrics
  [type theme]
  {:color (if (and (or (= :missing-keypair type)
                       (= :watch-only type))
                   (= :light theme))
            colors/neutral-80-opa-60
            colors/white-opa-70)})

(defn separator
  [type theme]
  {:width             2
   :height            2
   :border-radius     20
   :background-color  (if (and (or (= :missing-keypair type)
                                   (= :watch-only type))
                               (= :light theme))
                        colors/neutral-80-opa-20
                        colors/white-opa-40)
   :margin-horizontal 4})

(defn add-account-container
  [{:keys [theme metrics? pressed?]}]
  {:width              161
   :height             (if metrics? 88 68)
   :border-color       (colors/theme-colors
                        (if pressed? colors/neutral-40 colors/neutral-30)
                        (if pressed? colors/neutral-70 colors/neutral-80)
                        theme)
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
  [{:keys [width height watch-only? theme]}]
  {:width            width
   :height           height
   :background-color (if (and watch-only? (= :light theme)) colors/neutral-80-opa-5 colors/white-opa-10)
   :border-radius    6})

(def loader-container
  {:flex-direction :row
   :align-items    :center})

(def metrics-icon-container
  {:margin-left 4})

(def gradient-view
  {:position      :absolute
   :bottom        0
   :top           0
   :left          0
   :right         0
   :border-radius 16
   :z-index       -1})
