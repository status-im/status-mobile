(ns status-im.ui.components.styles
  (:require [status-im.ui.components.colors :as colors]))

(def color-transparent "transparent")
(def color-blue "#7099e6")
(def color-blue4 "#4360df") ; colors/blue
(def color-blue4-transparent "rgba(67, 96, 223, 0.10)")
(def color-blue6 "#3745AF")
(def color-blue-transparent "#7099e632")
(def color-black "#000000")
(def color-purple "#a187d5")
(def color-gray-transparent "rgba(0, 0, 0, 0.4)")
(def color-gray4-transparent "rgba(147, 155, 161, 0.2)")
(def color-gray "#838c93de")
(def color-gray2 "#8f838c93")
(def color-gray3 "#00000040")
(def color-gray4 "#939ba1")
(def color-gray5 "#d9dae1")
(def color-gray6 "#212121")
(def color-gray7 "#9fa3b4")
(def color-gray9 "#E9EBEC")
(def color-dark "#49545d")
(def color-white "white")
(def color-white-transparent "#ffffff66")
(def color-white-transparent-1 "#f1f1f11a")
(def color-white-transparent-3 "#FFFFFF1A")
(def color-white-transparent-4 "#FFFFFF33")
(def color-white-transparent-5 "#FFFFFF8C")
(def color-light-blue "#628fe3")
(def color-light-blue-transparent "#628fe333")
(def color-dark-blue-2 "#1f253f")
(def color-light-gray "#EEF2F5")
(def color-light-gray3 "#e8ebec")
(def color-light-gray6 "#BAC1C6")
(def color-red "red")
(def color-red-2 "#d84b4b")
(def color-light-red "#e86363")
(def color-green-3 "#44d058")
(def color-green-3-light "rgba(68, 208, 88, 0.2)")
(def color-green-4 "#0dcd8d")

(def color-separator "#D6D6D6")

(def text1-color color-black)
(def text2-color color-gray)
(def text4-color color-gray4)
(def icon-dark-color color-dark)
(def icon-gray-color color-gray7)
(def icon-red-color color-red-2)
(def online-color color-light-blue)
(def new-messages-count-color color-blue-transparent)
(def chat-background color-light-gray)
(def separator-color "#0000001f")
(def default-chat-color color-purple)

;;rgb 237 241 243

(def flex
  {:flex 1})

(def create-icon
  {:font-size 20
   :height    22
   :color     :white})

(def icon-back
  {:width  8
   :height 14})

(def icon-default
  {:width  24
   :height 24})

(def icon-add
  {:width           24
   :height          24
   :color           colors/blue})

(def icon-add-illuminated
  {:width           24
   :height          24
   :color           colors/blue
   :container-style {:background-color (colors/alpha colors/blue 0.12)
                     :border-radius    32
                     :width            32
                     :height           32
                     :display          :flex
                     :justify-content  :center
                     :align-items      :center}})

(def icon-ok
  {:width  18
   :height 14})

(def icon-qr
  {:width  23
   :height 22})

(def button-input-container
  {:flex           1
   :flex-direction :row})

(def button-input
  {:flex           1
   :flex-direction :column})

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
