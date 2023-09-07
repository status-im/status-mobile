(ns status-im2.contexts.emoji-picker.style
  (:require [quo2.components.profile.showcase-nav.style :as showcase-nav.style]
            [quo2.foundations.colors :as colors]
            [react-native.safe-area :as safe-area]
            [status-im2.contexts.emoji-picker.constants :as constants]))

(def flex-spacer {:flex 1})

(def category-nav-height (+ (safe-area/get-bottom) showcase-nav.style/height))

(def search-input-container
  {:padding-horizontal 20
   :padding-bottom     12})

(defn section-header
  [theme]
  {:background-color (colors/theme-colors colors/white colors/neutral-95 theme)
   :z-index          1
   :margin-bottom    constants/emoji-section-header-margin-bottom})

(def emoji-row-container
  {:padding-horizontal constants/emoji-row-padding-horizontal
   :padding-bottom     constants/emoji-row-separator-height
   :flex-direction     :row
   :overflow           :hidden})

(defn emoji-container
  [last-item-on-row?]
  (cond-> {:height       constants/emoji-size
           :width        constants/emoji-size
           :margin-right constants/emoji-item-margin-right}

    last-item-on-row?
    (dissoc :margin-right)))

(def list-container
  {:padding-bottom showcase-nav.style/height})

(def empty-results
  {:margin-top 100})

(def category-container
  {:height   category-nav-height
   :overflow :hidden
   :position :absolute
   :left     0
   :right    0
   :bottom   0
   :z-index  1})

(def category-blur-container
  {:height   category-nav-height
   :overflow :hidden})
