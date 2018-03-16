(ns status-im.ui.screens.main-tabs.styles
  (:require [status-im.ui.components.styles :as styles]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.styles :refer [defnstyle]]))

(def tabs-height (if platform/ios? 52 56))
(def tab-height (dec tabs-height))

(def tabs-container
  {:flex-direction   :row
   :height           tabs-height
   :background-color styles/color-white
   :border-top-width 1
   :border-top-color styles/color-light-gray3})

(def tab-container
  {:height          tabs-height
   :justify-content :center
   :align-items     :center})

(defnstyle tab-title [active?]
  {:ios        {:font-size 11}
   :android    {:font-size 12}
   :text-align :center
   :color      (if active?
                 styles/color-blue4
                 styles/color-gray4)})

(defn tab-icon [active?]
  {:color (if active? styles/color-blue4 styles/color-gray4)})

(def counter-container
  {:position :absolute
   :top      4})

(def counter
  {:margin-left 18})