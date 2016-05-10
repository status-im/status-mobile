(ns syng-im.components.discovery.styles
  (:require [syng-im.components.styles :refer [font
                                               title-font
                                               color-white
                                               chat-background
                                               online-color
                                               selected-message-color
                                               separator-color
                                               text1-color
                                               text2-color
                                               toolbar-background1]]))

;; common

(def row-separator
  {:borderBottomWidth 1
   :borderBottomColor "#eff2f3"})

(def row
  {:flexDirection "row"})

(def column
  {:flexDirection "column"})

;; discovery.cljs

(def discovery-search-input
  {:flex       1
   :marginLeft 18
   :lineHeight 42
   :fontSize   14
   :fontFamily "Avenir-Roman"
   :color      "#9CBFC0"})

(def discovery-title
  {:color      "#000000de"
   :alignSelf "center"
   :textAlign  "center"
   :fontFamily "sans-serif"
   :fontSize   16})

(def discovery-toolbar
  {:backgroundColor "#eef2f5"
   :elevation 0})

(def discovery-subtitle
  {:color      "#8f838c93"
   :fontFamily "sans-serif-medium"
   :fontSize   14})

(def section-spacing
  {:paddingLeft   30
   :paddingTop    15
   :paddingBottom 15})

;; discovery_popular.cljs

(def carousel-page-style
  {:borderRadius  1
   :shadowColor   "black"
   :shadowRadius  1
   :shadowOpacity 0.8
   :elevation     2
   :marginBottom  10})

;; discovery_populat_list.cljs

(def tag-name
  {:color "#7099e6"
   :fontFamily "sans-serif-medium"
   :fontSize   14
   :paddingRight 5
   :paddingBottom 2
   :alignItems "center"
   :justifyContent "center"})

(def tag-name-container
  {:flexDirection "column"
   :backgroundColor "#eef2f5"
   :borderRadius 5
   :padding 4})

(def tag-count
  {:color "#838c93"
   :fontFamily "sans-serif"
   :fontSize   12
   :paddingRight 5
   :paddingBottom 2
   :alignItems "center"
   :justifyContent "center"})

(def tag-count-container
  {:flex 0.2
   :flexDirection "column"
   :alignItems "flex-end"
   :paddingTop 10
   :paddingRight 9})

(def popular-list-container
  {:flex 1
   :backgroundColor "white"
   :paddingLeft 10
   :paddingTop 16})

(def popular-list
  {:backgroundColor "white"
   :paddingTop 13})

;; discover_popular_list_item.cjls

(def popular-list-item
  {:flexDirection "row"
   :paddingTop 10
   :paddingBottom 10})

(def popular-list-item-status
  {:color "black"
   :fontFamily "sans-serif"
   :lineHeight 22
   :fontSize 14})

(def popular-list-item-name
  {:color "black"
   :fontFamily "sans-serif-medium"
   :fontSize 14
   :lineHeight 24})

(def popular-list-item-name-container
  {:flex 0.8
   :flexDirection "column"})

(def popular-list-item-avatar-container
  {:flex 0.2
   :flexDirection "column"
   :alignItems "center"
   :paddingTop 5})

(def popular-list-item-avatar
  {:resizeMode "contain"
   :borderRadius 150
   :width 40
   :height 40})

;; discovery_recent

(def recent-list
  {:backgroundColor "white"
   :paddingLeft 15})

;; discovery_tag

(def discovery-tag-container
  {:flex            1
   :backgroundColor "#eef2f5"})

(def tag-title
  {:color "#7099e6"
   :fontFamily "sans-serif-medium"
   :fontSize   14
   :paddingRight 5
   :paddingBottom 2})

(def tag-title-container
  {:backgroundColor "#eef2f5"
   :flexWrap :wrap
   :borderRadius 5
   :padding 4})

(def icon-back
  {:width      8
   :height     14})

(def icon-search
  {:width  17
   :height 17})