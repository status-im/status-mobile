(ns status-im.ui.screens.discover.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.styles :as styles]))

;; Common

(def background-color styles/color-light-gray)

(def row-separator
  {:border-bottom-width 1
   :border-bottom-color styles/color-light-gray4})

(def row
  {:flex            1
   :flex-direction  :row
   :align-items     :center
   :justify-content :space-between
   :margin-bottom   10})

(def column
  {:flex-direction :column})

(def empty-view
  {:flex             1
   :background-color styles/color-white
   :align-items      :center
   :justify-content  :center})

(def title
  {:padding         16
   :flex-direction  :row
   :justify-content :space-between})

(defstyle title-text
  {:ios     {:color          styles/color-steel
             :font-size      13}
   :android {:color     styles/color-gray2
             :font-size 14}})

;; NOTE(oskarth): Hacky use of defstyle to get
;; platform specific styles in place where they belong
(defstyle subtitle-text-augment
  {:ios     {:uppercase? false}
   :android {:uppercase? true}})

(defstyle discover-item-status-text
  {:ios     {:font-size      14
             :letter-spacing -0.1}
   :android {:line-height 22
             :font-size   16}})

(def discover-list-item-name-container
  {:flex            1
   :padding-right   30
   :flex-direction  :row
   :justify-content :flex-start
   :align-items     :center})

(def discover-list-item-name
  {:margin-left 7
   :color       styles/color-black
   :font-size   14})

;; TODO(oskarth): These rules should be pulled out into more custom styles, not
;; generic enough for discover-list-item
(def discover-list-item
  {:flex-direction :column
   :padding-bottom 16
   :margin-right   10
   :top            1})

(def discover-list-item-full
  {:flex-direction    :column
   :margin-top        16
   :margin-horizontal 16
   :margin-bottom     12})

;; TODO(oskarth): Style too specific for full view, refactor
(def discover-list-item-second-row
  {:flex            1
   :flex-direction  :row
   :align-items     :center
   :justify-content :space-between
   :padding-top     25})

(defstyle discover-list-item-avatar-container
  {:flex-direction :column})

(def popular-container
  {:background-color background-color})

(def carousel-page-style
  {})

(def tag-button
  {:color           styles/color-blue
   :font-size       14
   :padding-right   5
   :padding-bottom  2
   :align-items     :center
   :justify-content :center})

(def tag-name
  {:color            styles/color-blue
   :background-color :white
   :font-size        14
   :align-items      :center
   :justify-content  :center})

(def tag-count
  {:color           styles/color-gray
   :font-size       12
   :padding-right   6
   :padding-bottom  2
   :align-items     :center
   :justify-content :center})

(def tag-count-container
  {:flex           0.2
   :flex-direction :column
   :align-items    :flex-end
   :padding-top    6
   :padding-right  9})

(def separator
  {:background-color styles/color-gray11
   :height           4
   :margin-top       2
   :margin-bottom    2})

;; Popular list item

(defstyle popular-list-container
  {:flex             1
   :background-color :white
   :padding-top      18
   :padding-left     16
   :ios              {:border-radius 3
                      :border-width  1
                      :border-color  styles/color-white}
   :android          {:border-radius 4
                      :margin-top    2
                      :margin-bottom 4
                      :margin-right  2}})

(def chat-button-container
  {:justify-content  :center
   :align-items      :center
   :background-color styles/color-blue4-transparent
   :border-radius    8})

(defstyle chat-button-inner
  {:flex-direction :row
   :padding-top    7
   :padding-left   7
   :padding-right  8
   :padding-bottom 5})

;; NOTE(goranjovic): Another hacky use of defstyle to get
;; platform specific styles in place where they belong
(defstyle chat-button-text-case
  {:ios     {:uppercase? false}
   :android {:uppercase? true}})

(defstyle chat-button-text
  {:color   styles/color-blue4
   :ios     {:font-size 15}
   :android {:font-size   14
             :font-weight :bold}})

;; discover_recent

(def status-list-outer
  {:background-color background-color})

(def status-list-inner
  {:background-color :white
   :margin-top       4})

;; All dapps

(def all-dapps-container
  {:flex             1
   :margin-top       16
   :background-color background-color})

(def all-dapps-flat-list
  {:flex-direction   :column
   :flex-wrap        :wrap
   :justify-content  :center
   :align-items      :center
   :margin-top       8
   :background-color styles/color-white})

(def all-dapps-flat-list-item
  {:margin          10
   :width           90
   :height          140
   :justify-content :center
   :align-items     :center})

(def dapps-list-item-name-container
  {:background-color styles/color-white
   :flex-direction   :column
   :align-items      :center})

(def dapps-list-item-name
  {:margin-left 7
   :padding     4
   :margin      4
   :color       styles/text1-color
   :font-size   14
   :text-align  :center})

(defstyle dapps-list-item-avatar-container
  {:flex-direction :column
   :padding        4
   :margin         4
   :align-items    :center
   :ios            {:padding-top     0
                    :bottom          -4
                    :justify-content :flex-end}})

(def dapp-preview-container
  {:background-color styles/color-white
   :margin-top       16
   :margin-bottom    4})

(def dapp-preview-flat-list
  {:justify-content  :center
   :flex-direction   :row
   :margin-left      8
   :margin-vertical  0
   :background-color styles/color-white})

;; Discover tag

(def discover-tag-toolbar
  {:border-bottom-color styles/color-light-gray5
   :border-bottom-width 1})

(def tag-title-container
  {:height           68
   :margin-left      16
   :align-items      :center
   :justify-content  :flex-start
   :flex-direction   :row
   :background-color styles/color-light-gray})

(defstyle tag-view
  {:margin-horizontal 2
   :padding           10
   :padding-bottom    8
   :height            36
   :background-color  styles/color-white
   :justify-content   :center
   :align-items       :center
   :flex-direction    :column
   :ios               {
                       :border-color  styles/color-light-blue6}
   :android           {:border-radius 4}})

(def tag-title
  {:color            styles/color-blue4
   :background-color styles/color-white
   :font-size        14})

(def icon-back
  {:width  8
   :height 14})

(def icon-search
  {:width  17
   :height 17})

(def discover-container
  {:flex             1
   :background-color styles/color-light-gray})

(def list-container
  {:flex 1})

(def search-icon
  {:width  17
   :height 17})

(defn title-action-text  [active?]
  {:color (if active?
            styles/color-blue
            styles/color-gray-transparent)})

(def recent-statuses-preview-container
  {:background-color background-color})

(def recent-statuses-preview-content
  {:border-radius    4
   :padding-top      18
   :padding-left     16
   :margin-top       2
   :margin-bottom    4
   :margin-right     2
   :background-color :white})

(def public-chats-container
  {:background-color styles/color-white})

(def public-chats-item-container
  {:flex-direction :row
   :padding        16})

(def public-chats-icon-width-ratio
  0.15)

(def public-chats-icon-container
  {:flex public-chats-icon-width-ratio})

(defn public-chats-icon [color]
  {:width            50
   :height           50
   :border-radius    25
   :background-color color
   :flex-direction   :row
   :justify-content  :center
   :align-items      :center})

(def public-chats-icon-text
  {:font-size 25})

(def public-chats-item-inner
  {:flex           (- 1 public-chats-icon-width-ratio)
   :margin-left    10
   :flex-direction :column})

(def public-chats-item-name-container
  {:flex-direction :row})

(def public-chats-item-name-text
  {:font-size   16
   :margin-left 5})

(def dapp-details-inner-container
  {:flex-direction   :column})

(defstyle dapp-details-header
  {:flex-direction   :row
   :justify-content  :flex-start
   :align-items      :center
   :height           80
   :background-color styles/color-white
   :margin-top       4
   :android          {:elevation 2}})

(def dapp-details-icon
  {:flex             0.2
   :background-color styles/color-white
   :margin-left      10})

(def dapp-details-name-container
  {:flex             0.8
   :background-color styles/color-white})

(defstyle dapp-details-name-text
  {:android {:font-size 16
             :color     styles/color-gray6}
   :ios     {:font-size 17}})

(defstyle dapp-details-action-container
  {:flex-direction   :row
   :margin-top       15
   :margin-bottom    15
   :align-items      :center
   :background-color styles/color-white
   :android          {:height    64
                      :elevation 2}
   :ios              {:height 72}})

(def dapp-details-action-icon-container
  {:padding-left     10
   :flex             0.2
   :justify-content  :center
   :align-items      :center
   :background-color styles/color-white})

(defstyle dapp-details-open-icon-background
  {:ios {:background-color styles/color-blue4-transparent
         :height           40
         :width            40
         :border-radius    20
         :justify-content  :center
         :align-items      :center}})

(defstyle dapp-details-open-icon
  {:width       24
   :height      24})

(def dapp-details-action-name-container
  {:flex             0.80
   :background-color styles/color-white
   :flex-direction   :row
   :justify-content  :flex-start
   :align-items      :center})

(defstyle dapp-details-action-name-text
  {:color       styles/color-blue4
   :android     {:font-size 16
                 :color     styles/color-gray6}
   :ios         {:font-size 17}})

(defstyle dapp-details-section-container
  {:background-color styles/color-white
   :android          {:elevation 2}})

(def dapp-details-section-title-container
  {:background-color styles/color-white
   :padding          10})

(defstyle dapp-details-section-title-text
  {:color   styles/color-gray
   :ios     {:font-size 14}
   :android {:font-size 12}})

(def dapp-details-section-body-container
  {:background-color styles/color-white
   :padding-left     10
   :padding-right    10
   :padding-bottom   16})

(defstyle dapp-details-section-content-text
  {:ios     {:font-size 17}
   :android {:font-size 16
             :color     styles/color-gray6}})

(def empty-section-container
  {:flex-direction   :row
   :justify-content  :center
   :align-items      :center
   :padding-vertical 50
   :margin-right     6})

(def empty-section-image
  {:height 70
   :width  70})

(def empty-section-description
  {:flex-direction :column
   :margin-left    6})

(def empty-section-title-text
  {:font-size 15})

(def empty-section-body-text
  {:margin-top 2
   :font-size  14})

;; TODO(oskarth): Copy of existing style, generalize - discover-container overloaded
(def all-recent-container all-dapps-container)
(def all-popular-container all-dapps-container)

;; TODO(goranjovic): Using the same style in dapp-details screen - reconcile later
(def dapp-details-container all-dapps-container)
(def discover-tag-container all-dapps-container)
