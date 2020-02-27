(ns status-im.ui.screens.profile.components.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.styles :as styles]))

;; profile header elements

(def profile-header-display
  {:flex-direction  :row
   :justify-content :flex-start
   :align-items     :center})

(def profile-header-edit
  {:flex-direction  :column
   :justify-content :center})

(def profile-name-text
  {:typography  :header
   :line-height 28
   :text-align  :left})

(def profile-name-text-with-subtitle
  {:margin-vertical 5
   :typography      :header
   :line-height     28
   :text-align      :left})

(def profile-three-words
  {:color       colors/gray})

(styles/def profile-name-input-text
  {:text-align :center
   :flex       1
   :desktop    {:height 20
                :width 200}
   :ios        {:margin-top          1
                :height              45
                :border-bottom-width 1
                :border-bottom-color colors/black-transparent}
   :android    {:border-bottom-width 2
                :border-bottom-color colors/blue}})

(def profile-header-name-container
  {:flex            1
   :justify-content :center
   :align-items     :flex-start
   :margin-left     16})

(def profile-header-name-container-with-subtitle
  {:flex            1
   :justify-content :flex-start
   :align-items     :flex-start
   :align-self      :stretch
   :margin-left     16})

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
  {:flex            1
   :flex-direction  :row
   :justify-content :space-between})

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

;; sheets

(def sheet-text
  {:color       colors/gray
   :padding     24
   :line-height 22
   :font-size   15})
