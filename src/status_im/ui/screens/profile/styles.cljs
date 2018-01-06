(ns status-im.ui.screens.profile.styles
  (:require [status-im.ui.components.styles
             :as styles
             :refer
             [color-black
              color-gray4
              color-gray5
              color-light-blue
              color-light-gray
              color-white
              text1-color]]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.styles :refer [defstyle]]))

(def profile
  {:flex             1
   :background-color color-light-gray
   :flex-direction   :column})

(def profile-form
  {:background-color color-white
   :padding          16})

(def edit-my-profile-form
  {:background-color color-white
   :flex             1})

(defstyle profile-info-container
  {:background-color color-white})

(def profile-info-item-button
  {:padding 16})

(def status-prompt
  {:padding-left  16
   :padding-right 16
   :padding-top   6})

(defstyle status-prompt-text
  {:color   color-gray4
   :ios     {:font-size      14
             :line-height    25
             :letter-spacing -0.2}
   :android {:line-height 18
             :font-size   12}})

(def profile-status-container
  {:background-color color-light-gray
   :margin-top       16
   :border-radius    4
   :padding          16
   :max-height       114})

(def profile-badge
  {:flex-direction :row})

(def edit-profile-badge
  {:flex-direction :row
   :padding-left   24})

(def context-menu-custom-styles
  {:optionsContainer {:margin-top 78}})

(def edit-profile-name-container
  {:flex        1
   :padding-top 30})

(def edit-profile-icon-container
  {:padding-top 25})

(def edit-name-title
  {:color   color-gray4
   :ios     {:font-size      13
             :letter-spacing -0.1}
   :android {:font-size 12}})

(defstyle profile-name-text
  {:ios     {:font-size      17
             :letter-spacing -0.2}
   :android {:color     color-black
             :font-size 16}})

(def profile-badge-name-container
  {:flex            1
   :justify-content :center
   :padding-left    16})

(def profile-activity-status-container
  {:margin-top 4})

(defstyle profile-activity-status-text
  {:color   color-gray4
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

(defstyle profile-setting-title
  {:color   color-gray4
   :ios     {:font-size      14
             :letter-spacing -0.2}
   :android {:font-size 12}})

(defstyle profile-setting-text
  {:ios     {:font-size      17
             :letter-spacing -0.2}
   :android {:font-size 16
             :color     color-black}})

(defstyle profile-setting-spacing
  {:ios     {:height 10}
   :android {:height 7}})

(def profile-setting-text-empty
  (merge profile-setting-text
         {:color color-gray4}))

(def info-item-separator
  {:margin-left 16})

(defstyle network-settings
  {:padding-horizontal 16
   :flex-direction     :row
   :align-items        :center
   :background-color   color-white
   :android            {:height 72}
   :ios                {:height 64}})

(defstyle offline-messaging-settings
  {:padding-horizontal 16
   :flex-direction     :row
   :align-items        :center
   :background-color   color-white
   :android            {:height 72}
   :ios                {:height 64}})

(def network-settings-text
  (merge {:flex 1}
         profile-setting-text))

(def offline-messaging-settings-text
  (merge {:flex 1}
         profile-setting-text))

(def edit-line-color
  (if platform/ios?
    (str color-gray5 "80")
    color-gray5))

(def profile-focus-line-color
  color-light-blue)

(defstyle profile-name-input
  {:color   text1-color
   :ios     {:font-size      17
             :padding-bottom 0
             :line-height    17
             :letter-spacing -0.2}
   :android {:font-size      16
             :line-height    16
             :padding-top    5
             :height         30
             :padding-bottom 0}})

(defstyle profile-status-input
  {:line-height  24                                         ;;TODO doesnt' work for multiline because a bug in the RN
   :color        text1-color
   :padding-left 0
   :ios          {:font-size      17
                  :padding-bottom 0
                  :padding-top    0
                  :height         60
                  :letter-spacing -0.2}
   :android      {:font-size           16
                  :padding-top         5
                  :height              74
                  :text-align-vertical :top
                  :padding-bottom      0}})

(defstyle profile-status-text
  {:color       text1-color
   :line-height 24
   :ios         {:font-size      17
                 :letter-spacing -0.2}
   :android     {:font-size 16}})

(defstyle edit-profile-status
  {:background-color   color-light-gray
   :border-radius      4
   :height             90
   :padding-horizontal 16
   :padding-bottom     16
   :margin-left        16
   :margin-right       16
   :ios                {:padding-top 10
                        :margin-top  10}
   :android            {:padding-top 13
                        :margin-top  13}})

(def add-a-status
  (merge profile-status-text
         {:color color-gray4}))

(def network-info {:background-color :white})
