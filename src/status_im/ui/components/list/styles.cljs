(ns status-im.ui.components.list.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def item
  {:flex-direction :row
   :justify-content :center})

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
  {:font-size   16
   :color       colors/black})

(def primary-text
  (merge primary-text-base
         {:padding-top 12}))

(def primary-text-only
  (merge primary-text-base
         {:padding-vertical 16}))

(def secondary-text
  {:font-size   14
   :color       colors/gray
   :padding-top 4})

(def image-size 40)

(def item-image
  {:width  image-size
   :height image-size})

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

(def horizontal-margin 12)
(def vertical-margin 12)

(def left-item-wrapper
  {:justify-content :center
   :margin-left     horizontal-margin
   :margin-vertical vertical-margin})

(def content-item-wrapper
  {:flex              1
   :justify-content   :center
   :margin-horizontal horizontal-margin})

(def right-item-wrapper
  {:justify-content :center
   :margin-right    12})

(def base-separator
  {:height           1
   :background-color colors/gray-light})

(def separator
  (merge
   base-separator
   {:margin-left 70}))

(defstyle list-header-footer-spacing
  {:android {:background-color colors/white
             :height           8}})

(defstyle section-header
  {:font-size       14
   :color           colors/gray
   :margin-left     16
   :margin-top      16
   :android         {:margin-bottom 3}
   :ios             {:margin-bottom 10}})

(def section-header-container
  {:background-color colors/white})

(def action-list
  {:background-color colors/blue})

(def action
  {:background-color colors/white-light-transparent
   :border-radius    50})

(def action-disabled
  {:background-color colors/gray-lighter})

(def action-label
  {:color colors/white})

(def action-label-disabled
  {:color colors/gray})

(def action-separator
  {:height           1
   :background-color colors/white-light-transparent
   :margin-left      70})

(def list-with-label-wrapper
  {:margin-top        26
   :margin-horizontal 16})

(def label
  {:color colors/gray})
