(ns status-im.ui.screens.profile.styles
  (:require [status-im.ui.components.styles :as styles]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform])
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

(def status-prompt
  {:padding-left  16
   :padding-right 16
   :padding-top   6})

(defstyle status-prompt-text
  {:color   colors/gray
   :ios     {:font-size      14
             :line-height    25
             :letter-spacing -0.2}
   :android {:line-height 18
             :font-size   12}})

(def profile-status-container
  {:background-color colors/gray
   :margin-top       16
   :border-radius    8
   :padding          16
   :max-height       114})

(def profile-badge
  {:flex-direction  :column
   :justify-content :center
   :align-items     :center})

(def profile-badge-edit
  {:flex-direction  :column
   :justify-content :center})

(def modal-menu
  {:align-items :center})

(def context-menu-custom-styles
  {:optionsContainer {:margin-top 78}})

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

(defstyle offline-messaging-settings
  {:padding-horizontal 16
   :flex-direction     :row
   :align-items        :center
   :background-color   colors/white
   :android            {:height 72}
   :ios                {:height 64}})

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

(def edit-line-color
  (if platform/ios?
    (str styles/color-gray5 "80")
    styles/color-gray5))

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

(defstyle profile-status-input
  {:line-height  24                                         ;;TODO doesnt' work for multiline because a bug in the RN
   :color        colors/black
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
  {:color       colors/black
   :line-height 24
   :ios         {:font-size      17
                 :letter-spacing -0.2}
   :android     {:font-size 16}})

(defstyle edit-profile-status
  {:background-color   styles/color-light-gray
   :border-radius      8
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
         {:color colors/gray}))

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
