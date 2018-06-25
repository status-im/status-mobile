(ns status-im.chat.styles.message.datemark
  (:require [status-im.ui.components.styles :as common]))

(def datemark-wrapper
  {:flex        1
   :align-items :center})

(def datemark
  {:margin-top       16
   :height           22})

(def datemark-text
  {:color          common/color-gray4
   :letter-spacing -0.2
   :font-size      15})
