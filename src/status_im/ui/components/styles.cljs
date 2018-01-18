(ns status-im.ui.components.styles
  (:require [status-im.utils.platform :as platform]))

;; TODO(oskarth): Make a palette of all these colors

(def color-transparent "transparent")
(def color-blue "#7099e6")
(def color-blue2 "#5b6dee")
(def color-blue3 "#424fae")
(def color-blue4 "#4360df")
(def color-blue4-faded "rgba(67,96,222,0.87)")
(def color-blue4-transparent "rgba(67, 96, 223, 0.10)")
(def color-blue5 "#3c56c8")
(def color-blue6 "#3745AF")
(def color-blue-transparent "#7099e632")
(def color-black "#000000")
(def color-purple "#a187d5")
(def color-gray-transparent-light "rgba(0, 0, 0, 0.1)")
(def color-gray-transparent-medium-light "rgba(0, 0, 0, 0.2)")
(def color-gray-transparent "rgba(0, 0, 0, 0.4)")
(def color-gray4-transparent "rgba(147, 155, 161, 0.2)")
(def color-gray10-transparent "rgba(184, 193, 199, 0.5)")
(def color-gray "#838c93de")
(def color-gray2 "#8f838c93")
(def color-gray3 "#00000040")
(def color-gray4 "#939ba1")
(def color-gray5 "#d9dae1")
(def color-gray6 "#212121")
(def color-gray7 "#9fa3b4")
(def color-gray8 "#6E777E")
(def color-gray9 "#E9EBEC")
(def color-gray10 "#9BA3A8")
(def color-gray11 "#EEF1F5")
(def color-dark "#49545d")
(def color-steel "#838b91")
(def color-white "white")
(def color-white-transparent "#ffffff66")
(def color-white-transparent-1 "#f1f1f11a")
(def color-white-transparent-2 "#fefefe21")
(def color-white-transparent-3 "#FFFFFF1A")
(def color-white-transparent-4 "#FFFFFF33")
(def color-white-transparent-5 "#FFFFFF8C")
(def color-white-transparent-6 "rgba(255, 255, 255, 0.6)")
(def color-light-blue "#628fe3")
(def color-light-blue-transparent "#628fe333")
(def color-light-blue2 "#eff3fc")
(def color-light-blue3 "#a0bcf0")
(def color-light-blue4 "#f1f4f5")
(def color-light-blue5 "#d9dff9")
(def color-light-blue6 "#e3ebfa")
(def color-light-blue7 "#dcd6fb")
(def color-dark-blue-1 "#252c4a")
(def color-dark-blue-2 "#1f253f")
(def color-dark-blue-3 "#191f37")
(def color-dark-blue-4 "#6e00e4")
(def color-light-gray "#EEF2F5")
(def color-light-gray2 "#ececf0")
(def color-light-gray3 "#e8ebec")
(def color-light-gray4 "#eff2f3")
(def color-light-gray5 "#D7D7D7")
(def color-light-gray6 "#BAC1C6")
(def color-red "red")
(def color-red-2 "#d84b4b")
(def color-red-3 "#FFC1BD")
(def color-red-4 "#8A3832")
(def color-light-red "#e86363")
(def color-light-red2 "#f47979")
(def color-green-1 "#a8f4d4")
(def color-green-2 "#448469")
(def color-green-3 "#44d058")
(def color-green-3-light "rgba(68, 208, 88, 0.2)")
(def color-green-4 "#0dcd8d")
(def color-cyan "#7adcfb")

(def color-separator "#D6D6D6")

(def text1-color color-black)
(def text1-disabled-color "#555555")
(def text2-color color-gray)
(def text3-color color-blue)
(def text4-color color-gray4)
(def icon-dark-color color-dark)
(def icon-gray-color color-gray7)
(def icon-red-color color-red-2)
(def online-color color-light-blue)
(def new-messages-count-color color-blue-transparent)
(def chat-background color-light-gray)
(def selected-message-color "#E4E9ED")
(def selected-contact-color color-light-blue2)
(def separator-color "#0000001f")
(def default-chat-color color-purple)

;;rgb 237 241 243

(def flex
  {:flex 1})

(def icon-search
  {:width  24
   :height 24})

(def create-icon
  {:fontSize 20
   :height   22
   :color    :white})

(def icon-back
  {:width  8
   :height 14})

(def icon-default
  {:width  24
   :height 24})

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
  {:width  12
   :height 12})

(def white-form-text-input
  {:marginLeft -4
   :fontSize   14
   :color      color-white})

(def button-input-container
  {:flex          1
   :flexDirection :row})

(def button-input
  {:flex          1
   :flexDirection :column})

(def modal
  {:position :absolute
   :left     0
   :top      0
   :right    0
   :bottom   0})

(def main-container
  {:background-color color-white
   :flex             1})

(def border-radius 8)

;; TODO(goranjovic): replace all platform conditional uppercase styling with a reference to this var
(def uppercase?
  (condp = platform/platform
    "android" true
    "ios"     false
    false))
