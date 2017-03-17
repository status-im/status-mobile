(ns status-im.profile.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.components.styles :refer [color-white
                                                 color-gray4
                                                 color-gray5
                                                 color-light-gray
                                                 color-light-blue
                                                 color-light-blue-transparent
                                                 text1-color]]
            [status-im.utils.platform :as p]))

(defn ps-profile [item]
  (get-in p/platform-specific [:component-styles :profile item]))

(def profile
  {:flex             1
   :background-color color-light-gray
   :flex-direction   :column})

(def profile-form
  {:background-color color-white
   :padding-bottom   16})

(def my-profile-form
  {:background-color color-white
   :padding-bottom   24})

(def edit-my-profile-form
  {:background-color color-white
   :flex             1})

(defstyle profile-info-container
  {:background-color color-white
   :ios              {:margin-top 16}
   :android          {:margin-top 12}})

(defstyle profile-actions-container
  {:android {:padding-top    8
             :padding-bottom 8}})

(def profile-bage
  (merge (ps-profile :profile-bage)
         {:align-items :center}))

(def edit-profile-bage
  {:flex-direction :row
   :align-items    :center
   :padding-left   24
   :padding-top    25})

(def profile-name-container
  {:margin-top 12})

(defstyle edit-profile-name-container
  {:flex 1
   :ios     {:padding-left 32
             :padding-top  11}
   :android {:padding-top  16
             :padding-left 16}})

(def edit-name-title
  (merge (ps-profile :edit-name-title)
         {:color color-gray4}))

(def edit-status-title
  edit-name-title)

(def profile-name-text
  (ps-profile :profile-name-text))

(def profile-status-container
  {:margin-top 4})

(def profile-status-text
  (merge (ps-profile :profile-status-text)
         {:color color-gray4}))

(def profile-setting-item
  (merge (ps-profile :profile-setting-item)
         {:flex-direction :row
          :align-items    :center}))

(def profile-setting-text-container
  {:flex          1
   :padding-right 20})

(def profile-setting-title
  (merge (ps-profile :profile-setting-title)
         {:color color-gray4}))

(def profile-setting-text
  (ps-profile :profile-setting-text))

(def profile-setting-spacing
  (ps-profile :profile-setting-spacing))

(def add-to-contacts
  {:margin-top       24
   :margin-left      16
   :margin-right     16
   :background-color color-light-blue
   :border-radius    4
   :height           52
   :align-items     :center
   :justify-content :center})

(def in-contacts
  (merge add-to-contacts
         {:flex-direction  :row
          :padding-right   40
          :padding-left    16
          :justify-content :flex-start
          :background-color color-light-blue-transparent}))

(def in-contacts-inner
  {:align-items :center
   :flex        1})

(def add-to-contacts-text
  (merge (ps-profile :add-to-contacts-text)
         {:color color-white}))

(def in-contacts-text
  (merge add-to-contacts-text
         {:color color-light-blue}))

(def info-item-separator
  {:margin-left      16})

(def form-separator
  (merge (ps-profile :form-separator)
         {:height           1
          :background-color color-gray5
          :opacity          0.5}))

(def profile-name-wrapper
  {:padding-top    0
   :margin-top     0
   :margin-bottom  0
   :height         42
   :padding-bottom 0})

(defstyle profile-status-wrapper
  {:padding-top    0
   :margin-bottom  0
   :height         85
   :padding-bottom 1
   :ios            {:margin-top 3}
   :android        {:margin-top 1}})

(def edit-line-color
  (if p/ios?
    (str color-gray5 "80")
    color-gray5))

(def profile-focus-line-color
  color-light-blue)

(def profile-focus-line-height
  (get-in p/platform-specific [:component-styles :text-field-focus-line-height]))

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
  {:height      82
   :line-height 24;;TODO doesnt' work for multiline because bug in the RN
   :android     {:padding-top  0}})

(def edit-profile-status
  {:padding-left 16
   :padding-top  35})

