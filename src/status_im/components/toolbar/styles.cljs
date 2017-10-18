(ns status-im.components.toolbar.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.components.styles :as styles]
            [status-im.utils.platform :as p]))

(def toolbar-background1 styles/color-white)

(def toolbar-height 56)
(def toolbar-icon-width 24)
(def toolbar-icon-height 24)
(def toolbar-icon-spacing 24)

(defnstyle toolbar [background-color flat?]
  {:flex             0
   :flex-direction   :row
   :align-items      :center
   :justify-content  :space-between
   :background-color (or background-color toolbar-background1)
   :elevation        (if flat? 0 2)
   :android          {:height 55}
   :ios              {:height 56}})

(defnstyle toolbar-nav-actions-container
  [actions]
  {:flex-direction :row
   :margin-left    4})

(defstyle toolbar-container
  {:flex       1
   :android    {:padding-left 18}
   :ios        {:align-items  :center}})

(def toolbar-title-container
  {:flex           1
   :flex-direction :column
   :margin-left    6})

(defstyle toolbar-title-text
  {:color          styles/text1-color
   :letter-spacing -0.2
   :font-size      17
   :ios            {:text-align "center"}})

(def toolbar-border-container
  (get-in p/platform-specific [:component-styles :toolbar-border-container]))

(def toolbar-border
  (get-in p/platform-specific [:component-styles :toolbar-border]))

(def toolbar-actions
  {:flex           0
   :flex-direction :row})

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
   :color               styles/color-black
   :ios                 {:padding-left   8
                         :padding-top    2
                         :letter-spacing -0.2}})

(def action-default
  {:width  24
   :height 24})

(def nav-item-button
  {:padding-vertical   16
   :padding-horizontal 12})

(def nav-item-text
  {:padding-vertical   18
   :padding-horizontal 16})

(defstyle item
  {:ios     {:marginHorizontal 12
             :marginVertical   16}
   :android {:margin 16}})

(def item-text
  {:color     styles/color-blue4
   :font-size 17})

(def toolbar-text-action-disabled {:color styles/color-gray7})

(def item-text-white-background {:color styles/color-blue4})

;;TODO(goranjovic) - Breaks the toolbar title into new line on smaller screens
;;e.g. see Discover > Popular hashtags on iPhone 5s
(def ios-content-item {:position :absolute :right 40 :left 40})
