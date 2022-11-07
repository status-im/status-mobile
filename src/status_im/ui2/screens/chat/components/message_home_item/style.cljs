(ns status-im.ui2.screens.chat.components.message-home-item.style
<<<<<<< HEAD
<<<<<<< HEAD
  (:require [quo2.foundations.colors :as colors]
            [quo2.foundations.typography :as typography]))
=======
  (:require [quo2.foundations.colors :as colors]))
>>>>>>> 5fcc08fd3... refactor
=======
  (:require [quo2.foundations.colors :as colors]
            [quo2.foundations.typography :as typography]))
>>>>>>> 8e74397a9... lint

(defn container []
  {:margin-top         8
   :margin-horizontal  8
   :padding-vertical   8
   :padding-horizontal 12
   :border-radius      12
   :flex-direction     :row
   :align-items        :center})

(defn group-chat-icon [color]
  {:width            32
   :height           32
   :background-color color
   :justify-content  :center
   :align-items      :center
   :border-radius    16})

(defn count-container []
  {:width            8
   :height           8
   :border-radius    4
   :position         :absolute
   :right            26
   :top              16
   :background-color (colors/theme-colors colors/neutral-40 colors/neutral-60)})
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 8e74397a9... lint

(defn timestamp []
  (merge typography/font-regular typography/label
         {:color       (colors/theme-colors colors/neutral-50 colors/neutral-40)
          :margin-top  3
          :margin-left 8}))
<<<<<<< HEAD
=======
>>>>>>> 5fcc08fd3... refactor
=======
>>>>>>> 8e74397a9... lint
