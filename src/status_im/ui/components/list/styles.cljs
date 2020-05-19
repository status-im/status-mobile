(ns status-im.ui.components.list.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.styles :as styles]))

(def item
  {:flex-direction     :row
   :justify-content    :center
   :padding-horizontal 16})

(def item-content-view
  {:flex            1
   :flex-direction  :column
   :justify-content :center})

(def item-checkbox
  {:flex            1
   :flex-direction  :column
   :align-items     :center
   :justify-content :center})

(def primary-text-base
  {:font-size 16})

(def primary-text-only
  (merge primary-text-base
         {:padding-vertical 16}))

(def image-size 40)

(def item-image
  {:width         image-size
   :height        image-size})

(def icon-size 24)
(def icon-wrapper-size (+ icon-size (* 2 8)))

(def item-icon-wrapper
  {:width           icon-wrapper-size
   :height          icon-wrapper-size
   :align-items     :center
   :justify-content :center})

(def item-icon
  {:width  icon-size
   :height icon-size})

(def left-item-wrapper
  {:justify-content :center
   :margin-vertical 12})

(def content-item-wrapper
  {:flex              1
   :justify-content   :center
   :margin-horizontal 16})

(def right-item-wrapper
  {:justify-content :center})

(def base-separator
  {:height           1
   :background-color colors/black-transparent})

(def separator
  (merge
   base-separator
   {:margin-left 64}))

(styles/def section-header
  {:font-size       14
   :color           colors/gray
   :margin-left     16
   :margin-top      16
   :android         {:margin-bottom 3}
   :ios             {:margin-bottom 10}})

(def section-header-container {})