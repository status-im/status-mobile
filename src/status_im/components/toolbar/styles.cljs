(ns status-im.components.toolbar.styles
  (:require [status-im.components.styles :refer [text1-color
                                                 color-white
                                                 color-light-gray]]))

(def toolbar-background1 color-white)
(def toolbar-background2 color-light-gray)

(def toolbar-height 56)
(def toolbar-icon-width 32)
(def toolbar-icon-spacing 8)

(def toolbar-gradient
  {:height 4})

(defn toolbar [background-color]
  {:flexDirection   :row
   :backgroundColor (or background-color toolbar-background1)
   :height          toolbar-height
   :elevation       2})

(defn toolbar-nav-actions-container [actions]
  {:width          (if (and actions (> (count actions) 0))
                     (-> (+ toolbar-icon-width toolbar-icon-spacing)
                         (* (count actions))
                         (+ toolbar-icon-spacing)))
   :flex-direction "row"})

(def toolbar-nav-action
  {:width           toolbar-height
   :height          toolbar-height
   :align-items     :center
   :justify-content :center})

(def toolbar-title-container
  {:flex           1
   :alignItems     :center
   :justifyContent :center})

(def toolbar-title-text
  {:margin-top -2.5
   :color      text1-color
   :font-size  16})

(def toolbar-actions-container
  {:flex-direction "row"
   :margin-left    8})

(def toolbar-action
  {:width           toolbar-icon-width
   :height          toolbar-height
   :margin-right    toolbar-icon-spacing
   :align-items     :center
   :justify-content :center})