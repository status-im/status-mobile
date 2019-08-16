(ns status-im.ui.components.toolbar.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def toolbar-height 56)
(def toolbar-icon-width 24)
(def toolbar-icon-height 24)
(def toolbar-icon-spacing 24)

(def toolbar
  {:height 55
   :flex   1})

(def toolbar-title-container
  {:justify-content :center
   :align-items     :center
   :flex-direction  :column
   :margin-left     6})

(def toolbar-title-text
  {:typography :title-bold
   :text-align :center})

(defn toolbar-actions-container [actions-count custom]
  (merge {:flex-direction :row}
         (when-not custom {:margin-right 4})
         (when (and (zero? actions-count) (not custom))
           {:width (+ toolbar-icon-width toolbar-icon-spacing)})))

(def touchable-area
  {:width 56
   :height 56
   :justify-content :center
   :align-items :center})

(def action-default
  {:width  24
   :height 24})

(def item-text
  {:color colors/blue})

(defstyle item-text-action
  {:color colors/blue})

(def toolbar-text-action-disabled
  {:color colors/gray})

(def item-text-white-background
  {:color colors/blue})

(def icon-add
  {:width  24
   :height 24
   :color  colors/blue})

(def icon-add-illuminated
  {:width           24
   :height          24
   :color           colors/blue
   :container-style {:background-color colors/blue-transparent-10
                     :border-radius    28
                     :display          :flex
                     :justify-content  :center
                     :align-items      :center}})
