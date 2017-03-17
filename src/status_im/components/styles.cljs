(ns status-im.components.styles)

(def color-transparent "transparent")
(def color-blue "#7099e6")
(def color-blue-transparent "#7099e632")
(def color-black "#000000")
(def color-purple "#a187d5")
(def color-gray "#838c93de")
(def color-gray2 "#8f838c93")
(def color-gray3 "#00000040")
(def color-gray4 "#939ba1")
(def color-gray5 "#d9dae1")
(def color-steel "#838b91")
(def color-white "white")
(def color-light-blue "#628fe3")
(def color-light-blue-transparent "#628fe333")
(def color-light-blue2 "#eff3fc")
(def color-light-gray "#EEF2F5")
(def color-red "red")
(def color-light-red "#e86363")

(def color-separator "#D6D6D6")

(def text1-color color-black)
(def text1-disabled-color "#555555")
(def text2-color color-gray)
(def text3-color color-blue)
(def text4-color color-gray4)
(def online-color color-light-blue)
(def new-messages-count-color color-blue-transparent)
(def chat-background color-light-gray)
(def selected-message-color "#E4E9ED")
(def selected-contact-color color-light-blue2)
(def separator-color "#0000001f")
(def default-chat-color color-purple)

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

(def confirm-button
  {:flex-direction   :row
   :height           52
   :justify-content  :center
   :align-items      :center
   :background-color color-light-blue})

(def context-menu-options
  {:customStyles {:optionsContainer {:elevation      2
                                     :margin-top     30
                                     :padding-top    8
                                     :width          164
                                     :padding-bottom 8}
                  :optionWrapper    {:padding-left    16
                                     :padding-right   16
                                     :justify-content :center
                                     :height          48}}})

(defn context-menu-text [destructive?]
  {:font-size   15
   :line-height 20
   :color       (if destructive? color-light-red text1-color)})
