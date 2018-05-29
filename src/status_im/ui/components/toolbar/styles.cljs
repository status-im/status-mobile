(ns status-im.ui.components.toolbar.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def toolbar-background colors/white)

(def toolbar-height 56)
(def toolbar-icon-width 24)
(def toolbar-icon-height 24)
(def toolbar-icon-spacing 24)

(defnstyle toolbar [background-color flat?]
  {:flex             0
   :flex-direction   :row
   :align-items      :center
   :justify-content  :space-between
   :background-color (or background-color toolbar-background)
   :elevation        (if flat? 0 2)
   :android          {:height 55}
   :ios              {:height 56}})

(def toolbar-nav-actions-container
  {:flex-direction :row
   :margin-left    4})

(defstyle toolbar-container
  {:flex        1
   :align-items :center})

(def toolbar-title-container
  {:flex           1
   :flex-direction :column
   :margin-left    6})

(defstyle toolbar-title-text
  {:color          colors/black
   :letter-spacing -0.2
   :font-size      17
   :ios            {:text-align     :center}
   :android        {:text-align     :left
                    :margin-left    22}})

(def toolbar-actions
  {:flex           0
   :flex-direction :row})

(defn toolbar-actions-container [actions-count custom]
  (merge {:flex-direction :row}
         (when-not custom {:margin-right 4})
         (when (and (zero? actions-count) (not custom))
           {:width (+ toolbar-icon-width toolbar-icon-spacing)})))

(def toolbar-action
  {:flex            1
   :align-items     :center
   :justify-content :center})

(def action-default
  {:width  24
   :height 24})

(defn nav-item-button [unread-messages?]
  {:margin-left  13
   :margin-right (if unread-messages? -5 13)})

(defstyle item
  {:ios     {:padding-horizontal 12
             :padding-vertical   16}
   :android {:padding 16}})

(def item-text
  {:color     colors/blue
   :font-size 17})

(defstyle item-text-action
  {:color   colors/blue
   :ios     {:font-size      15
             :letter-spacing -0.2}
   :android {:font-size      14
             :letter-spacing 0.5}})

(def toolbar-text-action-disabled {:color colors/gray})

(def item-text-white-background {:color colors/blue})

;;TODO(goranjovic) - Breaks the toolbar title into new line on smaller screens
;;e.g. see Discover > Popular hashtags on iPhone 5s
(def ios-content-item {:position :absolute :right 40 :left 40})

(def counter-container
  {:top 3})
