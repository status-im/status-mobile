(ns status-im.ui.screens.chat.styles.input.gap
  (:require [status-im.ui.components.colors :as colors]))

(def gap-container
  {:align-self          :stretch
   :margin-top          24
   :margin-bottom       24
   :height              48
   :align-items         :center
   :justify-content     :center
   :border-color        colors/black-transparent
   :border-top-width    1
   :border-bottom-width 1})

(def label-container
  {:flex            1
   :align-items     :center
   :justify-content :center
   :text-align      :center})

(defn gap-text [connected?]
  {:text-align :center
   :color      (if connected?
                 colors/blue
                 colors/gray)})

(def touchable
  {:height 48})

(def date
  {:typography :caption
   :color      colors/gray})
