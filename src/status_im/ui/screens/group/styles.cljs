(ns status-im.ui.screens.group.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.styles :as common]))

(defn toolbar-icon [enabled?]
  {:width   20
   :height  18
   :opacity (if enabled? 1 0.3)})

(def group-container
  {:flex             1
   :flex-direction   :column
   :background-color common/color-white})

(defstyle reorder-list-container
  {:flex    1
   :android {:padding-top 16}})

(def add-button-container
  {:margin-left 16})

(defstyle group-name-text
  {:letter-spacing -0.1
   :color          common/color-gray4
   :ios            {:font-size 13}
   :android        {:font-size 12}})

(defstyle members-text
  {:color   common/color-gray4
   :ios     {:letter-spacing -0.2
             :font-size      16}
   :android {:font-size 14}})

(defstyle members-text-count
  {:margin-left 8
   :color       common/color-gray4
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
  {:width           40
   :height          40
   :align-items     :center
   :justify-content :center
   :ios             {:background-color "#628fe333"
                     :border-radius    50}})
(def add-icon
  {:align-items :center
   :width       24
   :height      24})

(defstyle add-group-text
  {:color   common/color-light-blue
   :ios     {:color          common/color-light-blue
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
         {:color common/color-light-red}))

(defstyle delete-group-prompt-text
  {:color       common/color-gray4
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

(def contact
  {:padding-left 0})

(defstyle settings-group-container
  {:ios     {:margin-top 25}
   :android {:margin-top 23}})

(defstyle settings-group-item
  {:padding-left   16
   :flex-direction :row
   :align-items    :center
   :ios            {:height 64}
   :android        {:height 56}})

(defstyle delete-icon-container
  {:width           40
   :height          40
   :align-items     :center
   :justify-content :center
   :ios             {:background-color "#d84b4b33"
                     :border-radius    50}})

(def order-item-container
  {:background-color common/color-white})

(defstyle order-item-inner-container
  {:flex-direction :row
   :align-items    :center
   :android        {:padding-top      17
                    :padding-bottom   15
                    :min-height       56
                    :background-color common/color-white}
   :ios            {:padding-vertical 22
                    :min-height       63}})

(defstyle order-item-label
  {:padding-left 16
   :flex-shrink  1
   :android      {:font-size   16
                  :color       common/color-black
                  :line-height 24}
   :ios          {:font-size      17
                  :line-height    20
                  :letter-spacing -0.2}})

(defstyle order-item-contacts
  {:padding-left 8
   :color        common/color-gray4
   :ios          {:font-size      17
                  :line-height    20
                  :letter-spacing -0.2}
   :android      {:font-size   16
                  :line-height 24}})

(defstyle order-item-icon
  {:android {:padding-horizontal 16}
   :ios     {:padding-horizontal 20}})

(def order-item-separator-wrapper
  {:background-color common/color-white})

(def order-item-separator
  {:height           1
   :background-color common/color-gray5
   :ios              {:margin-left 16
                      :opacity     0.5}})

(defstyle toolbar-container
  {:flex    1
   :android {:padding-left 18}
   :ios     {:align-items :center}})

(def separator
  {:background-color common/color-gray5
   :height           1
   :opacity          0.5})

(def list-view-container
  {:flex       1
   :margin-top 10})
