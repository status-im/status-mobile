(ns status-im.contacts.styles
  (:require [status-im.components.styles :refer [text1-color
                                                 text2-color
                                                 text3-color
                                                 color-white
                                                 color-separator
                                                 color-gray2
                                                 color-gray]]
            [status-im.components.toolbar.styles :refer [toolbar-background1 toolbar-background2]]
            [status-im.utils.platform :as p]))

;; Contacts list

(def toolbar-shadow
  {:height           2
   :background-color toolbar-background2})

(def toolbar-actions
  {:flex-direction :row})

(def contact-groups
  {:flex             1
   :background-color toolbar-background2})

(def contacts-list-container
  (merge (get-in p/platform-specific [:component-styles :main-tab-list])
         {:flex 1}))

(def empty-contact-groups
  (merge contact-groups
         {:align-items     :center
          :justify-content :center}))

(def empty-contacts-icon
  {:height 62
   :width  62})

(def empty-contacts-text
  {:margin-top 12
   :font-size  16
   :color      color-gray2})

(def contacts-list
  {:backgroundColor color-white})

(def contact-group
  {:flex-direction :column})

(def contact-group-subtitle
  {:margin-left 16})

(def contact-group-count
  {:flex        1
   :margin-left 8
   :opacity     0.6})

(def contact-group-header-gradient-top
  {:flexDirection   :row
   :height          3
   :backgroundColor toolbar-background2})

(def contact-group-header-gradient-top-colors
  ["rgba(24, 52, 76, 0.165)"
   "rgba(24, 52, 76, 0.03)"
   "rgba(24, 52, 76, 0.01)"])

(def contact-group-header-gradient-bottom
  {:flexDirection   :row
   :height          2
   :backgroundColor toolbar-background2})

(def contact-group-header-gradient-bottom-colors
  ["rgba(24, 52, 76, 0.01)"
   "rgba(24, 52, 76, 0.05)"])

(def show-all
  {:flexDirection   :row
   :alignItems      :center
   :height          56
   :backgroundColor color-white})

(def show-all-text
  {:marginLeft    72
   :fontSize      14
   :color         text3-color
   ;; ios only:
   :letterSpacing 0.5})

(def contact-separator-container
  {:background-color color-white})

(def contact-container
  {:flex-direction   :row
   :background-color color-white})

(def letter-container
  {:paddingTop  11
   :paddingLeft 20
   :width       56})

(def letter-text
  {:fontSize 24
   :color    text3-color})

(def contact-photo-container
  {:marginTop  4
   :marginLeft 12})

(def option-inner-container
  {:flex                1
   :flex-direction      :row
   :height              56
   :background-color    color-white
   :border-bottom-color color-separator
   :border-bottom-width 0.5})

(def option-inner
  {:width       48
   :height      48
   :margin-top  4
   :margin-left 12})

(def option-inner-image
  {:width  24
   :height 18
   :top    16
   :left   13})

(def group-icon
  (assoc option-inner-image
    :tint-color color-gray))

(def spacing-top
  {:background-color color-white
   :height           8})

(def spacing-bottom
  {:background-color color-white
   :height           8})

(def contact-inner-container
  {:flex            1
   :flexDirection   :row
   :height          56
   :margin-right    16
   :backgroundColor color-white})

(def info-container
  {:flex           1
   :flexDirection  :column
   :margin-left    12
   :justifyContent :center})

(def name-text
  {:fontSize 15
   :color    text1-color})

(def info-text
  {:marginTop 1
   :fontSize  12
   :color     text2-color})

(def more-btn
  {:width          24
   :height         56
   :margin-right   14
   :alignItems     :center
   :justifyContent :center})

(def search-btn
  {:width          24
   :height         56
   :margin-right   24
   :alignItems     :center
   :justifyContent :center})

; New contact

(def contact-form-container
  {:flex            1
   :color           :white
   :backgroundColor :white})

(def gradient-background
  {:position :absolute
   :top      0
   :right    0
   :bottom   0
   :left     0})

(def form-container
  {:margin-left 16
   :margin-top  8
   :height      72})

(def address-explication-container
  {:flex         1
   :margin-top   30
   :paddingLeft  16
   :paddingRight 16})

(def address-explication
  {:textAlign :center
   :color     color-gray})

(def qr-input
  {:margin-right 42})

(def enter-address-icon
  {:margin-left   21
   :margin-right  21
   :margin-top    19
   :width         20
   :height        18})

(def scan-qr-icon
  {:margin-left   21
   :margin-right  20
   :margin-top    18
   :width         20
   :height        20})

;; Contacts search

(def search-container
  {:flex             1
   :background-color color-white})

(def search-empty-view
  {:flex             1
   :background-color color-white
   :align-items      :center
   :justify-content  :center})
