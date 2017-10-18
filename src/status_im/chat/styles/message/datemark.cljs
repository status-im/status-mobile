(ns status-im.chat.styles.message.datemark
  (:require [status-im.components.styles :as common]))

(def datemark-wrapper
  {:flex        1
   :align-items :center})

(def datemark
  {:opacity          0.5
   :margin-top       20
   :height           20})

(def datemark-text
  {:color     common/color-gray4
   :font-size 15})
