(ns status-im.discovery.styles
  (:require [status-im.components.styles :refer [color-white
                                                 color-gray2
                                                 chat-background
                                                 online-color
                                                 selected-message-color
                                                 separator-color
                                                 text1-color
                                                 text2-color
                                                 toolbar-background1]]))

;; common

(def row-separator
  {:border-bottom-width 1
   :border-bottom-color "#eff2f3"})

(def row
  {:flex-direction :row})

(def column
  {:flex-direction :column})

;; Toolbar

(def discovery-toolbar-content
  {:flex            1
   :align-items     :center
   :justify-content :center})

(def discovery-toolbar
  {:background-color "#eef2f5"
   :elevation        0})

(def discovery-search-input
  {:flex        1
   :align-self  "stretch"
   :margin-left 18
   :line-height 42
   :font-size   14
   :color       "#9CBFC0"})

(def discovery-title
  {:color       "#000000de"
   :align-self  :center
   :text-align  :center
   :font-size   16})

(def discovery-subtitle
  {:color     color-gray2
   :font-size 14})

(def section-spacing
  {:padding 16})

(def scroll-view-container
  {})

;; Popular

(def carousel-page-style
  {:borderRadius  1
   :shadowColor   "black"
   :shadowRadius  1
   :shadowOpacity 0.8
   :elevation     2
   :marginBottom  10})

(def tag-name
  {:color           "#7099e6"
   :font-size       14
   :padding-right   5
   :padding-bottom  2
   :align-items     :center
   :justify-content :center})

(def tag-name-container
  {:flex-direction   "column"
   :background-color "#eef2f5"
   :border-radius    5
   :padding          4})

(def tag-count
  {:color           "#838c93"
   :font-size       12
   :padding-right   5
   :padding-bottom  2
   :align-items     :center
   :justify-content :center})

(def tag-count-container
  {:flex           0.2
   :flex-direction "column"
   :align-items    "flex-end"
   :padding-top    10
   :padding-right  9})

(def popular-list-container
  {:flex             1
   :background-color :white
   :padding-left     10
   :padding-top      16})

(def popular-list
  {:background-color :white
   :padding-top      13})

;; Popular list item

(def popular-list-item
  {:flex-direction :row
   :padding-top    10
   :padding-bottom 10})

(def popular-list-item-status
  {:color       "black"
   :line-height 22
   :font-size   14})

(def popular-list-item-name
  {:color       "black"
   :font-size   14
   :line-height 24})

(def popular-list-item-name-container
  {:flex           0.8
   :flex-direction "column"})

(def popular-list-item-avatar-container
  {:flex           0.2
   :flex-direction "column"
   :align-items    :center
   :padding-top    5})

(def popular-list-item-avatar
  {:border-radius 18
   :width         36
   :height        36})

;; discovery_recent

(def recent-list
  {:background-color :white
   :padding-left     16})

;; Discovery tag

(def discovery-tag-container
  {:flex            1
   :backgroundColor "#eef2f5"})

(def tag-title-container
  {:flex           1
   :alignItems     "center"
   :justifyContent "center"
   :flex-direction "row"})

(def tag-title
  {:color         "#7099e6"
   :font-size      14
   :padding-right  5
   :padding-bottom 2})

(def tag-container
  {:backgroundColor "#eef2f5"
   :flexWrap        :wrap
   :borderRadius    5
   :padding         4
   :margin-left     2
   :margin-right    2})

(def icon-back
  {:width  8
   :height 14})

(def icon-search
  {:width  17
   :height 17})

(def discovery-container
  {:flex            1
   :backgroundColor :#eef2f5})

(def hamburger-icon
  {:width  16
   :height 12})

(def search-icon
  {:width  17
   :height 17})
