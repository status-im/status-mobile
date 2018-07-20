(ns status-im.ui.screens.main-tabs.styles
  (:require [status-im.ui.components.styles :as styles]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.styles :refer [defnstyle]]))

(def tabs-height
  (cond
    platform/android? 56
    platform/ios? 52
    platform/desktop? 68))

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
   :desktop    {:font-size 12
                :font-weight (if active? "600" :normal)}
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

(def unread-messages-icon
  {:position         :absolute
   :width            20
   :height           20
   :border-radius    20
   :left             18
   :top              10
   :justify-content  :center
   :align-items      :center
   :background-color colors/blue})

(defn unread-messages-text [large?]
  {:color     colors/white
   :font-size (if large? 10 10.9)})

