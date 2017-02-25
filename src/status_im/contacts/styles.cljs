(ns status-im.contacts.styles
  (:require [status-im.components.styles :refer [text1-color
                                                 text2-color
                                                 text3-color
                                                 text4-color
                                                 separator-color
                                                 color-white
                                                 color-light-gray
                                                 color-blue-transparent
                                                 color-gray2
                                                 color-gray]]
            [status-im.components.toolbar.styles :refer [toolbar-background1 toolbar-background2]]
            [status-im.utils.platform :as p]))

;; Contacts list

(def list-bottom-shadow
  ["rgba(24, 52, 76, 0.165)"
   "rgba(24, 52, 76, 0.03)"
   "rgba(24, 52, 76, 0.01)"])

(def list-top-shadow
  ["rgba(24, 52, 76, 0.01)"
   "rgba(24, 52, 76, 0.03)"])

(def list-separator
  {:border-bottom-width 1
   :border-bottom-color separator-color})

(def list-separator-wrapper
  {:background-color color-white
   :height           1
   :padding-left     16})

(def option-list-separator-wrapper  (merge list-separator-wrapper {:padding-left 16}))
(def contact-list-separator-wrapper (merge list-separator-wrapper {:padding-left 72}))

(def new-chat-options
  {:padding-top      8
   :padding-bottom   8
   :background-color color-white})

(def toolbar-shadow
  {:height           2
   :background-color toolbar-background2})

(def toolbar-actions
  {:flex-direction :row})

(def contact-groups
  {:flex             1
   :background-color toolbar-background2})

(def contacts-list-container
  {:flex          1
   :margin-bottom 10})

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
  {:backgroundColor color-light-gray})

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

(def contact-list-title-container
  (merge {:padding-left 16}
         (get-in p/platform-specific [:component-styles :new-chat :contact-list-title-container])))

(def contact-list-title
  (get-in p/platform-specific [:component-styles :new-chat :contact-list-title]))

(def contact-list-title-count
  {:color text4-color})

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
  (merge {:flex             1
          :background-color color-white}
         (get-in p/platform-specific [:component-styles :new-chat :option-inner-container])))

(def icon-container
  (merge {:width         40
          :height        40
          :border-radius 50}
         (get-in p/platform-specific [:component-styles :new-chat :option-icon-container])))

(def option-inner-image
  {:width  24
   :height 24
   :top    8
   :left   8})

(def group-icon
  option-inner-image)

(def option-name-text
  (get-in p/platform-specific [:component-styles :new-chat :option-name-text]))

(def spacing-top
  {:background-color color-white
   :height           (if p/ios? 0 8)})

(def spacing-bottom
  {:background-color color-white
   :height           (if p/ios? 0 8)})

(def contact-inner-container
  {:flex            1
   :flexDirection   :row
   :height          56
   :margin-right    16
   :backgroundColor color-white})

(def info-container
  {:flex           1
   :flexDirection  :column
   :margin-left    16
   :justifyContent :center})

(def name-text
  {:fontSize 16
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
  {:margin-left  21
   :margin-right 21
   :margin-top   19
   :width        20
   :height       18})

(def scan-qr-icon
  {:margin-left  21
   :margin-right 20
   :margin-top   18
   :width        20
   :height       20})

;; Contacts search

(def search-container
  {:flex             1
   :background-color color-white})

(def search-empty-view
  {:flex             1
   :background-color color-white
   :align-items      :center
   :justify-content  :center})
