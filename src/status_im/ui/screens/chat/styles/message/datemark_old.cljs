(ns status-im.ui.screens.chat.styles.message.datemark-old
  (:require [quo.design-system.colors :as colors]))

(def datemark-mobile
  {:flex        1
   :align-items :center
   :margin-top  16
   :height      22})

(defn datemark-text []
  {:color colors/gray})