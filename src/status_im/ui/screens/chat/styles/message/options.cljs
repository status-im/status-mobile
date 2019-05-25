(ns status-im.ui.screens.chat.styles.message.options
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.styles :as styles]
            [status-im.ui.components.colors :as colors]))

(defstyle row
  {:flex-direction     :row
   :background-color   :white
   :align-items        :center
   :padding-horizontal 16
   :ios                {:height 36}
   :desktop            {:height 36}
   :android            {:height 36}})

(def title
  {:padding-horizontal 16
   :padding-top        10
   :padding-bottom     10})

(def title-text
  {:font-weight "700"})

(def label
  {:padding-horizontal 16})

(def label-text
  {:typography :caption})

(def icon
  {:width            40
   :height           40
   :border-radius    20
   :align-items      :center
   :justify-content  :center})
