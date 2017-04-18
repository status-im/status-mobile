(ns status-im.new-group.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.components.styles :refer [color-white
                                                 color-blue
                                                 color-black
                                                 text1-color
                                                 text2-color
                                                 color-light-blue
                                                 color-light-red
                                                 color-light-gray
                                                 selected-contact-color
                                                 color-gray4
                                                 color-gray5]]
            [status-im.utils.platform :refer [platform-specific] :as p]))

(defn toolbar-icon [enabled?]
  {:width   20
   :height  18
   :opacity (if enabled? 1 0.3)})

(def group-container
  {:flex             1
   :flex-direction   :column
   :background-color color-white})

(def reorder-groups-container
  {:flex             1
   :flex-direction   :column
   :background-color color-light-gray})

(defstyle reorder-list-container
  {:flex    1
   :android {:padding-top 16}})

(def chat-name-container
  {:padding-left 16
   :margin-top   10})

(def group-name-container
  {:margin-top 10})

(def add-button-container
  {:margin-left 16})

(def group-chat-name-input
  {:font-size      17
   :padding-bottom 0
   :letter-spacing -0.2
   :color          text1-color})

(defstyle group-chat-topic-input
  {:font-size      14
   :line-height    16
   :color          text1-color
   :padding-left   13
   :ios            {:padding-bottom 0}})

(defstyle topic-hash-style
  {:width    10
   :position :absolute
   :android  {:top 8 :left 3}
   :ios      {:top 6 :left 3}})

(def topic-hash
  (merge group-chat-name-input
         topic-hash-style))

(def group-chat-name-wrapper
  {:padding-top    0
   :height         40
   :padding-bottom 0})

(defstyle group-name-text
  {:letter-spacing -0.1
   :color          color-gray4
   :ios            {:font-size 13}
   :android        {:font-size 12}})

(defstyle members-text
  {:color   color-gray4
   :ios     {:letter-spacing -0.2
             :font-size      16}
   :android {:font-size 14}})

(defstyle members-text-count
  {:margin-left 8
   :color       color-gray4
   :opacity     0.6
   :ios         {:letter-spacing -0.2
                 :font-size      16}
   :android     {:font-size 14}})

(def add-container
  {:flex-direction :row
   :align-items    :center
   :height         64
   :margin-top     12})

(defstyle settings-icon-container
  {:width            40
   :height           40
   :align-items      :center
   :justify-content  :center
   :ios              {:background-color "#628fe333"
                      :border-radius    50}})

(def add-icon
  {:align-items :center
   :width       24
   :height      24})

(defstyle add-group-text
  {:color   color-light-blue
   :ios     {:color          color-light-blue
             :letter-spacing -0.2
             :font-size      17
             :line-height    20}
   :android {:letter-spacing 0.5
             :font-size      16}})

(def settings-group-text
  add-group-text)

(def settings-group-text-container
  {:padding-left 16})

(def delete-group-text
  (merge add-group-text
         {:color color-light-red}))

(defstyle delete-group-prompt-text
  {:color       color-gray4
   :padding-top 5
   :ios         {:font-size      14
                 :letter-spacing -0.2}
   :android     {:font-size 12}})

(defstyle contact-container
  {:flex-direction  :row
   :justify-content :center
   :align-items     :center
   :ios             {:height 63}
   :android         {:height 56}})

(defstyle settings-group-container
  {:ios     {:margin-top 25}
   :android {:margin-top 23}})

(defstyle settings-group-item
  {:padding-left   16
   :flex-direction :row
   :align-items    :center
   :ios            {:height         64}
   :android        {:height         56}})

(defstyle delete-icon-container
  {:width            40
   :height           40
   :align-items      :center
   :justify-content  :center
   :ios              {:background-color "#d84b4b33"
                      :border-radius    50}})

(def order-item-container
  {:background-color color-white})

(defstyle order-item-inner-container
  {:flex-direction :row
   :align-items    :center
   :android        {:padding-top      17
                    :padding-bottom   15
                    :min-height       56
                    :background-color color-white}
   :ios            {:padding-vertical 22
                    :min-height       63}})

(defstyle order-item-label
  {:padding-left 16
   :flex-shrink  1
   :android      {:font-size   16
                  :color       color-black
                  :line-height 24}
   :ios          {:font-size      17
                  :line-height    20
                  :letter-spacing -0.2}})

(defstyle order-item-contacts
  {:padding-left 8
   :color        color-gray4
   :ios          {:font-size      17
                  :line-height    20
                  :letter-spacing -0.2}
   :android      {:font-size   16
                  :line-height 24}})

(defstyle order-item-icon
  {:android {:padding-horizontal 16}
   :ios     {:padding-horizontal 20}})

(def order-item-separator-wrapper
  {:background-color color-white})

(def order-item-separator
  {:height           1
   :background-color color-gray5
   :ios              {:margin-left 16
                      :opacity     0.5}})

(def toolbar-title-with-count-text
  {:color          text1-color
   :letter-spacing -0.2
   :font-size      17})

(def toolbar-title-with-count-text-count
  (merge toolbar-title-with-count-text
         {:color color-light-blue}))

(def toolbar-title-with-count
  {:flex-direction :row})

(def toolbar-title-with-count-container
  {:padding-left 6})

(def separator
  {:background-color color-gray5
   :height           1
   :opacity          0.5})

(def list-view-container
  {:flex       1
   :margin-top 10})




