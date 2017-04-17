(ns status-im.discover.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle]])
  (:require [status-im.components.styles :refer [color-white
                                                 color-light-gray
                                                 color-blue]]
            [status-im.components.toolbar.styles :refer [toolbar-background2]]
            [status-im.components.tabs.styles :as tabs-st]
            [status-im.utils.platform :as p]))

;; Common

(def row-separator
  {:border-bottom-width 1
   :border-bottom-color "#eff2f3"})

(def row
  {:flex-direction :row
   :margin-bottom  10})

(def column
  {:flex-direction :column})

(def empty-view
  {:flex             1
   :background-color color-white
   :align-items      :center
   :justify-content  :center})

(def section-spacing
  {:padding 16})

;; Popular

(def popular-container
  {:background-color toolbar-background2})

(def carousel-page-style
  {})

(def tag-name
  {:color           color-blue
   :font-size       14
   :padding-right   5
   :padding-bottom  2
   :align-items     :center
   :justify-content :center})

(def tag-count
  {:color           "#838c93"
   :font-size       12
   :padding-right   6
   :padding-bottom  2
   :align-items     :center
   :justify-content :center})

(def tag-count-container
  {:flex           0.2
   :flex-direction "column"
   :align-items    "flex-end"
   :padding-top    6
   :padding-right  9})

(def separator
  {:background-color "rgb(200, 199, 204)"
   :height           0.5})

;; Popular list item

(def popular-list-container
  {:flex             1
   :background-color :white
   :padding-top      18
   :padding-left     16})

(def popular-list-item
  {:flex-direction :row
   :padding-bottom 16
   :top            1})

(def popular-list-item-name
  {:color          "black"
   :font-size      15
   :padding-bottom 4})

(def popular-list-item-name-container
  {:flex           0.8
   :flex-direction "column"
   :padding-top    16})

(def popular-list-item-avatar-container
  {:flex           0.2
   :flex-direction "column"
   :align-items    :center
   :padding-top    16})

;; discover_recent

(def recent-container
  {:background-color toolbar-background2})

(def recent-list
  {:background-color :white
   :padding-left     16})

;; Discover tag

(def discover-tag-toolbar
  {:border-bottom-color "#D7D7D7"
   :border-bottom-width 1})

(def discover-tag-container
  {:flex            1
   :backgroundColor color-light-gray})

(def tag-title-scroll
  {:flex           1
   :alignItems     "center"
   :justifyContent "center"})

(def tag-title-container
  {:flex           1
   :alignItems     "center"
   :justifyContent "center"
   :flex-direction "row"})

(def tag-title
  {:color          color-blue
   :font-size      14
   :padding-right  5
   :padding-bottom 2})

(def icon-back
  {:width  8
   :height 14})

(def icon-search
  {:width  17
   :height 17})

(def discover-container
  {:flex            1
   :backgroundColor color-white})

(defnstyle list-container [tabs-hidden?]
  {:android {:margin-bottom 20}
   :ios     {:margin-bottom (if tabs-hidden? 20 (+ 16 tabs-st/tabs-height))}})

(def search-icon
  {:width  17
   :height 17})
