(ns status-im.ui.components.search-input.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.styles :as styles]))

(styles/def search-input
  {:flex 1
   :android {:margin  0
             :padding 0}})

(def search-input-height 56)

(defn search-container []
  {:height             search-input-height
   :flex-direction     :row
   :padding-horizontal 16
   :background-color   colors/white
   :align-items        :center
   :justify-content    :center})

(defn search-input-container []
  {:background-color colors/gray-lighter
   :flex             1
   :flex-direction   :row
   :height           36
   :align-items      :center
   :justify-content  :center
   :border-radius    8})