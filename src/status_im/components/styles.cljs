(ns status-im.components.styles)

(def color-transparent "transparent")
(def color-blue "#7099e6")
(def color-blue-transparent "#7099e632")
(def color-black "#000000de")
(def color-purple "#a187d5")
(def color-gray "#838c93de")
(def color-gray2 "#8f838c93")
(def color-gray3 "#00000040")
(def color-white :white)
(def color-light-blue "#bbc4cb")
(def color-light-blue-transparent "#bbc4cb32")
(def color-dark-mint "#5fc48d")
(def color-light-gray "#EEF2F5")

(def text1-color color-black)
(def text1-disabled-color "#555555")
(def text2-color color-gray)
(def text3-color color-blue)
(def text4-color color-white)
(def text5-color "#838c938f")
(def online-color color-blue)
(def new-messages-count-color color-blue-transparent)
(def chat-background color-light-gray)
(def selected-message-color "#E4E9ED")
(def separator-color "#0000001f")
(def toolbar-background1 color-white)
(def toolbar-background2 color-light-gray)
(def default-chat-color color-purple)

(def toolbar-height 56)

(def flex
  {:flex 1})

(def hamburger-icon
  {:width  16
   :height 12})

(def icon-search
  {:width  17
   :height 17})

(def create-icon
  {:fontSize 20
   :height   22
   :color    :white})

(def icon-back
  {:width  8
   :height 14})

(def icon-add
  {:width  14
   :height 14})

(def icon-ok
  {:width  18
   :height 14})

(def icon-qr
  {:width  23
   :height 22})

(def icon-scan
  {:width  18
   :height 18})

(def icon-plus
  {:width  18
   :height 18})

(def icon-close
  {:width 12
   :height 12})

(def form-text-input
  {:marginLeft -4
   :fontSize   14
   :color      text1-color})

(def white-form-text-input
  {:marginLeft -4
   :fontSize   14
   :color      color-white})

(def toolbar-title-container
  {:flex           1
   :alignItems     :center
   :justifyContent :center})

(def toolbar-title-text
  {:margin-top -2.5
   :color      text1-color
   :font-size  16})

(def button-input-container
  {:flex          1
   :flexDirection :row})

(def button-input
  {:flex          1
   :flexDirection :column})

(def toolbar-gradient
  {:height 4})