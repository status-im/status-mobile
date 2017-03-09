(ns status-im.chat.styles.datemark
  (:require [status-im.components.styles :as st]))

(def datemark-wrapper
  {:flex        1
   :align-items :center})

(def datemark
  {:opacity          0.5
   :margin-top       20
   :height           20})

(def datemark-text
  {:color     st/color-gray4
   :font-size 15})
