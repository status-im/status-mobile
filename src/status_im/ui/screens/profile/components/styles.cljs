(ns status-im.ui.screens.profile.components.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.styles :as styles]
            [status-im.ui.components.colors :as colors]))

;; profile header elements

(def profile-header-display
  {:flex-direction  :column
   :justify-content :center
   :align-items     :center})

(def profile-header-edit
  {:flex-direction  :column
   :justify-content :center})

(defstyle profile-name-text
  {:padding-vertical 8
   :text-align       :center
   :font-weight      "700"})

(defstyle profile-three-words
  {:font-size   12
   :text-align  :center
   :color       colors/gray})

(defstyle profile-name-input-text
  {:text-align  :center
   :flex        1
   :desktop     {:height 20
                 :width 200}
   :ios         {:margin-top          1
                 :height              45
                 :border-bottom-width 1
                 :border-bottom-color colors/gray-light}
   :android     {:border-bottom-width 2
                 :border-bottom-color colors/blue}})

(def profile-header-name-container
  {:flex            1
   :justify-content :center})

;; settings items elements

(def settings-item-separator
  {:margin-left 16})

(def settings-item
  {:padding-left   16
   :padding-right  8
   :flex           1
   :flex-direction :row
   :align-items    :center
   :height         52})

(def settings-item-icon
  {:background-color colors/blue-light
   :width            34
   :height           34
   :border-radius    34
   :margin-right     16
   :justify-content  :center
   :align-items      :center})

(def settings-item-text-wrapper
  {:flex             1
   :flex-direction   :row
   :justify-content  :space-between})

(def settings-item-text
  {:flex-wrap :nowrap})

(def settings-item-destructive
  {:color colors/red})

(def settings-item-disabled
  {:color colors/gray})

(def settings-item-value
  {:flex          1
   :flex-wrap     :nowrap
   :text-align    :right
   :padding-right 10
   :color         colors/gray})

(def settings-title
  {:color         colors/gray
   :margin-left   16
   :margin-top    18
   :font-size     14})

;; shared profile styles

(def modal-menu
  {:align-items :center})

(def profile
  {:flex           1
   :flex-direction :column})

(def profile-form
  {:padding-vertical 16})
