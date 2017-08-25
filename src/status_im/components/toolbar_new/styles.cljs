(ns status-im.components.toolbar-new.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.components.styles :as st]
            [status-im.utils.platform :as p]))

(def toolbar-background1 st/color-white)

(def toolbar-icon-width 24)
(def toolbar-icon-height 24)
(def toolbar-icon-spacing 24)

(defn toolbar-wrapper [background-color]
  {:backgroundColor (or background-color toolbar-background1)
   :elevation       2})

(defstyle toolbar
  {:flex-direction     :row
   :align-items        :center
   :android            {:height 55}
   :ios                {:height 56}})

(defnstyle toolbar-nav-actions-container
  [actions]
  {:flex-direction :row
   :margin-left    4
   :ios            {:width (when (and actions (pos? (count actions)))
                             (-> (+ toolbar-icon-width toolbar-icon-spacing)
                                 (* (count actions))))}})

(defstyle toolbar-title-container
  {:flex       1
   :android    {:padding-left 18}
   :ios        {:align-items  :center}})

(defstyle toolbar-title-text
  {:color          st/text1-color
   :letter-spacing -0.2
   :font-size      17
   :ios            {:text-align "center"}})

(def toolbar-border-container
  (get-in p/platform-specific [:component-styles :toolbar-border-container]))

(def toolbar-border
  (get-in p/platform-specific [:component-styles :toolbar-border]))

(defn toolbar-actions-container [actions-count custom]
  (merge {:flex-direction :row}
         (when-not custom {:margin-right 4})
         (when (and (zero? actions-count) (not custom))
           {:width (+ toolbar-icon-width toolbar-icon-spacing)})))

(def toolbar-action
  {:width           toolbar-icon-width
   :height          toolbar-icon-height
   :align-items     :center
   :justify-content :center})

(def toolbar-with-search
  {:background-color toolbar-background1})

(defstyle toolbar-with-search-content
  {:flex    1
   :android {:padding-left 18}
   :ios     {:align-items :center}})

(defstyle toolbar-search-input
  {:line-height         24
   :height              24
   :font-size           17
   :padding-top         0
   :padding-left        0
   :padding-bottom      0
   :text-align-vertical :center
   :color               st/color-black
   :ios                 {:padding-left   8
                         :padding-top    2
                         :letter-spacing -0.2}})

(def action-default
  {:width  24
   :height 24})

(def toolbar-button
  {:paddingVertical   16
   :paddingHorizontal 12})

(def toolbar-right-action
  {:color        st/color-blue4
   :font-size    17
   :margin-right 12})

