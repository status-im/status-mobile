(ns status-im2.contexts.chat.home.contact-request.style
  (:require [quo2.foundations.colors :as colors]))

(def contact-requests
  {:flex-direction     :row
   :margin             8
   :padding-horizontal 12
   :padding-vertical   8
   :align-items        :center})

(defn contact-requests-icon
  []
  {:justify-content :center
   :align-items     :center
   :width           32
   :height          32
   :border-radius   16
   :border-width    1
   :border-color    (colors/theme-colors colors/neutral-20 colors/neutral-80)})

(defn contact-requests-sheet
  []
  {:background-color (colors/theme-colors colors/neutral-10 colors/neutral-80)
   :width            32
   :height           32
   :border-radius    10
   :justify-content  :center
   :align-items      :center
   :margin-bottom    24})
