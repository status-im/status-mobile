(ns status-im.ui.screens.profile.styles
  (:require [status-im.ui.components.styles :as styles]
            [status-im.ui.components.colors :as colors])
  (:require-macros [status-im.utils.styles :refer [defstyle]]))

(def profile
  {:flex             1
   :background-color colors/white
   :flex-direction   :column})

(def profile-form
  {:background-color colors/white
   :padding          16})

(defstyle profile-info-container
  {:background-color colors/white})

(def profile-info-item-button
  {:padding 16})

(def profile-badge
  {:flex-direction  :column
   :justify-content :center
   :align-items     :center})

(def profile-badge-edit
  {:flex-direction  :column
   :justify-content :center})

(def modal-menu
  {:align-items :center})

(def edit-name-title
  {:color   colors/gray
   :ios     {:font-size      13
             :letter-spacing -0.1}
   :android {:font-size 12}})

(defstyle profile-name-text
  {:padding-vertical 14
   :font-size        15
   :text-align       :center
   :ios              {:letter-spacing -0.2}
   :android          {:color colors/black}})

(defstyle profile-name-input-text
  {:font-size  15
   :text-align :center
   :flex       1
   :ios        {:letter-spacing      -0.2
                :height              46
                :border-bottom-width 1
                :border-bottom-color styles/color-light-gray3}
   :android    {:color               colors/black
                :border-bottom-width 2
                :border-bottom-color styles/color-blue4}})

(def profile-badge-name-container
  {:flex            1
   :justify-content :center})

(def profile-activity-status-container
  {:margin-top 4})

(defstyle profile-activity-status-text
  {:color   colors/gray
   :ios     {:font-size      14
             :line-height    20
             :letter-spacing -0.2}
   :android {:font-size   15
             :line-height 20}})

(defstyle profile-setting-item
  {:flex-direction :row
   :align-items    :center
   :padding-left   16
   :ios            {:height 73}
   :android        {:height 72}})

(defn profile-info-text-container [options]
  {:flex          1
   :padding-right (if options 16 40)})

(defstyle profile-settings-title
  {:color         colors/gray
   :margin-left   16
   :margin-top    18
   :margin-bottom 20
   :font-size     14
   :ios           {:letter-spacing -0.2}})

(defstyle profile-info-title
  {:color         colors/gray
   :margin-left   16
   :font-size     14
   :ios           {:letter-spacing -0.2}})

(defstyle profile-setting-text
  {:ios     {:font-size      17
             :letter-spacing -0.2}
   :android {:font-size 16
             :color     colors/black}})

(defstyle profile-setting-spacing
  {:ios     {:height 10}
   :android {:height 7}})

(def profile-setting-text-empty
  (merge profile-setting-text
         {:color colors/gray}))

(def settings-item-separator
  {:margin-left 16})

(defstyle settings-item
  {:padding-horizontal 16
   :flex-direction     :row
   :align-items        :center
   :background-color   colors/white
   :height             52})

(defstyle settings-item-text
  {:flex      1
   :font-size 15
   :ios       {:letter-spacing -0.2}
   :android   {:color colors/black}})

(def settings-item-value
  {:padding-right 10
   :font-size     15
   :color         colors/gray})

(defstyle logout-text
  (merge settings-item-text
         {:color        colors/red}))

(defstyle profile-name-input
  {:color   styles/text1-color
   :ios     {:font-size      17
             :padding-bottom 0
             :line-height    17
             :letter-spacing -0.2}
   :android {:font-size      16
             :line-height    16
             :padding-top    5
             :height         30
             :padding-bottom 0}})

(def network-info {:background-color :white})

(def share-contact-code
  {:margin-horizontal 16
   :flex-direction    :row
   :justify-content   :space-between
   :align-items       :center
   :height            42
   :border-radius     8
   :background-color  styles/color-blue4-transparent})

(def share-contact-code-text-container
  {:padding-left    16
   :padding-bottom  1
   :flex            0.9
   :flex-direction  :row
   :justify-content :center
   :align-items     :center})

(def share-contact-code-text
  {:color     colors/blue
   :font-size 15})

(def share-contact-icon-container
  {:border-radius   50
   :flex            0.1
   :padding-right   5
   :align-items     :center
   :justify-content :center})
