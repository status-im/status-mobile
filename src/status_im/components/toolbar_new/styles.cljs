(ns status-im.components.toolbar-new.styles
  (:require [status-im.components.styles :refer [text1-color
                                                 color-white
                                                 color-light-gray
                                                 color-gray5
                                                 color-blue
                                                 color-black]]
            [status-im.utils.platform :as p]))

(def toolbar-background1 color-white)

(def toolbar-icon-width 24)
(def toolbar-icon-height 24)
(def toolbar-icon-spacing 24)

(def toolbar-gradient
  {:height 4})

(defn toolbar-wrapper [background-color]
  {:backgroundColor (or background-color toolbar-background1)
   :elevation       2})

(def toolbar
  (merge {:flex-direction :row}
         (get-in p/platform-specific [:component-styles :toolbar-new])))

(defn toolbar-nav-actions-container [actions]
  (let [center? (get-in p/platform-specific [:component-styles :toolbar-title-center?])]
    (merge {:flex-direction :row}
           (when center?
             {:width          (when (and actions (pos? (count actions)))
                                (-> (+ toolbar-icon-width toolbar-icon-spacing)
                                    (* (count actions))))}))))

(def toolbar-title-container
  (merge (get-in p/platform-specific [:component-styles :toolbar-title-container])
         {:flex           1
          :align-self     :stretch}))

(def toolbar-title-text
  {:color          text1-color
   :letter-spacing -0.2
   :font-size      17})

(def toolbar-border-container
  (get-in p/platform-specific [:component-styles :toolbar-border-container]))

(def toolbar-border
  (get-in p/platform-specific [:component-styles :toolbar-border]))

(defn toolbar-actions-container [actions-count custom]
  (merge {:flex-direction :row}
         (when (and (zero? actions-count) (not custom))
           {:width (+ toolbar-icon-width toolbar-icon-spacing)})))

(def toolbar-action
  {:width           toolbar-icon-width
   :height          toolbar-icon-height
   :margin-left     toolbar-icon-spacing
   :align-items     :center
   :justify-content :center})

(def toolbar-with-search
  {:background-color toolbar-background1})

(def toolbar-with-search-content
  (merge (get-in p/platform-specific [:component-styles :toolbar-with-search-content])
         {:flex 1}))

(def toolbar-search-input
  (merge (get-in p/platform-specific [:component-styles :toolbar-search-input])
         {:flex           1
          :padding-bottom 10
          :font-size      17
          :padding-top    0
          :align-self     :stretch
          :color          color-black}))

(def action-default
  {:width  24
   :height 24})

