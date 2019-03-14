(ns status-im.ui.screens.extensions.styles
  (:require [status-im.ui.components.colors :as colors])
  (:require-macros [status-im.utils.styles :refer [defstyle]]))

(def wrapper
  {:flex             1
   :background-color colors/white})

(defstyle extension-item
  {:flex-direction     :row
   :background-color   :white
   :align-items        :center
   :padding-horizontal 16
   :ios                {:height 64}
   :android            {:height 56}})

(def extension-item-inner
  {:flex               1
   :padding-horizontal 16})

(defstyle extension-item-name-text
  {:typography :title})

(defn mailserver-icon [connected?]
  {:width            40
   :height           40
   :border-radius    20
   :background-color (if connected?
                       colors/blue
                       colors/gray-light)
   :align-items      :center
   :justify-content  :center})

(def empty-list
  {:text-align :center})
