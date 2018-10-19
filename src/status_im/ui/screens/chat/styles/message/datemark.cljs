(ns status-im.ui.screens.chat.styles.message.datemark
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def datemark-wrapper
  {:flex        1
   :align-items :center})

(def datemark
  {:margin-top       16
   :height           22})

(defstyle datemark-text
  {:color     colors/gray
   :ios       {:letter-spacing -0.2}
   :font-size 15})
