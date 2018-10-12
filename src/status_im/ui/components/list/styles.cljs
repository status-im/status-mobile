(ns status-im.ui.components.list.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def item
  {:flex               1
   :flex-direction     :row
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
  {:font-size   16
   :color       colors/black})

(def primary-text
  primary-text-base)

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
   :background-color colors/gray-light})

(def separator
  (merge
   base-separator
   {:margin-left 64}))

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
   :margin-left      64})

(def list-with-label-wrapper
  {:margin-top        26
   :margin-horizontal 16})

(def label
  {:color colors/gray})

(def delete-button-width 100)

(def delete-icon-highlight
  {:position         :absolute
   :top              0
   :bottom           0
   :right            -800
   :width            800
   :background-color colors/red-light})

(def delete-icon-container
  {:flex            1
   :width           delete-button-width
   :justify-content :center
   :align-items     :center})
