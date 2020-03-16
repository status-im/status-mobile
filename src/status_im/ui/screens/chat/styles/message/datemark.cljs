(ns status-im.ui.screens.chat.styles.message.datemark
  (:require [status-im.ui.components.colors :as colors]))

(def datemark-wrapper
  {:flex        1
   :align-items :center})

(def datemark
  {:margin-top       16
   :height           22})

(def datemark-mobile
  {:flex        1
   :align-items :center
   :margin-top  16
   :height      22})

(defn datemark-text []
  {:color colors/gray})
