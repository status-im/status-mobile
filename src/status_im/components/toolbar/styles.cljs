(ns status-im.components.toolbar.styles
  (:require [status-im.components.styles :refer [text1-color
                                                 color-white
                                                 color-light-gray
                                                 color-blue
                                                 color-black]]))

(def toolbar-background1 color-white)
(def toolbar-background2 color-light-gray)

(def toolbar-height 56)
(def toolbar-icon-width 32)
(def toolbar-icon-spacing 8)

(def toolbar-gradient
  {:height 4})

(defn toolbar-wrapper [background-color]
  {:backgroundColor (or background-color toolbar-background1)
   :elevation       2})

(def toolbar
  {:flex-direction :row
   :height         toolbar-height})

(defn toolbar-nav-actions-container [actions]
  {:width          (when (and actions (> (count actions) 0))
                     (-> (+ toolbar-icon-width toolbar-icon-spacing)
                         (* (count actions))
                         (+ toolbar-icon-spacing)))
   :flex-direction "row"})

(def toolbar-nav-action
  {:width           toolbar-height
   :height          toolbar-height
   :align-items     :center
   :justify-content :center
   :padding-right   12})

(def toolbar-title-container
  {:flex           1
   :alignItems     :center
   :justifyContent :center})

(def toolbar-title-text
  {:margin-top 0
   :color      text1-color
   :font-size  16})

(defn toolbar-actions-container [actions-count custom]
  (merge {:flex-direction "row"
          :margin-left    toolbar-icon-spacing}
         (when (and (zero? actions-count) (not custom))
           {:width (+ toolbar-icon-width toolbar-icon-spacing)})))

(def toolbar-action
  {:width           toolbar-icon-width
   :height          toolbar-height
   :margin-right    toolbar-icon-spacing
   :align-items     :center
   :justify-content :center})

(def toolbar-with-search
  {:background-color toolbar-background2
   :elevation        0})

(def toolbar-with-search-content
  {:flex            1
   :align-items     :center
   :justify-content :center})

(def toolbar-search-input
  {:flex        1
   :align-self  :stretch
   :margin-left 18
   :margin-top  2
   :font-size   14
   :color       color-blue})

(def toolbar-with-search-title
  {:color       color-black
   :align-self  :center
   :text-align  :center
   :font-size   16})


;; Specific actions

(def action-hamburger
  {:width  16
   :height 12})

(def action-add
  {:width  17
   :height 17})

(def action-search
  {:width  17
   :height 17})

(def action-back
  {:width  24
   :height 24})
