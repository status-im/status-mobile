(ns status-im.new-group.styles
  (:require [status-im.components.styles :refer [color-white
                                                 color-blue
                                                 text1-color
                                                 text2-color
                                                 color-light-blue
                                                 color-light-red
                                                 color-light-gray
                                                 selected-contact-color
                                                 color-gray4
                                                 color-gray5]]
            [status-im.utils.platform :refer [platform-specific] :as p]))

(defn ps-reorder [item]
  (get-in platform-specific [:component-styles :reorder-groups item]))

(defn ps-new-group [item]
  (get-in platform-specific [:component-styles :new-group item]))

(defn toolbar-icon [enabled?]
  {:width   20
   :height  18
   :opacity (if enabled? 1 0.3)})

(def new-group-container
  {:flex             1
   :flex-direction   :column
   :background-color color-white})

(def reorder-groups-container
  {:flex             1
   :flex-direction   :column
   :background-color color-light-gray})

(def reorder-list-container
  (merge {:flex 1}
         (ps-reorder :reorder-list-container)))

(def chat-name-container
  {:margin-top  21
   :margin-left 16})

(def group-chat-name-input
  {:font-size      17
   :letter-spacing -0.2
   :color          text1-color})

(def group-chat-topic-input
  {:font-size    14
   :color        text1-color
   :padding-left 13})

(def topic-hash
  (merge group-chat-name-input
         {:width    10
          :height   16
          :position :absolute}
         (get-in platform-specific [:public-group-chat-hash-style])))

(def group-chat-focus-line-color
  (get-in platform-specific [:group-chat-focus-line-color]))

(def group-chat-focus-line-height
  (get-in platform-specific [:group-chat-focus-line-height]))

(def group-chat-name-wrapper
  {:padding-top    0
   :height         40
   :padding-bottom 0})

(def group-name-text
  (merge (ps-new-group :group-name-text)
         {:letter-spacing -0.1
          :color          color-gray4}))

(def members-container
  {:flex-direction :row
   :padding-top     20})

(def members-text
  (merge (ps-new-group :members-text)
         {:color          color-gray4}))

(def members-text-count
  (merge (ps-new-group :members-text-count)
         {:margin-left    8
          :color          color-gray4
          :opacity        0.6}))

(def add-container
  {:flex-direction :row
   :align-items    :center
   :height         64
   :margin-top     12})

(def add-icon-container
  {:width           40
   :align-items     :center
   :justify-content :center})

(def add-icon
  {:align-items :center
   :width       24
   :height      24})

(def add-text
  (merge (ps-new-group :add-text)
         {:color color-light-blue}))

(def delete-group-text
  (merge (ps-new-group :delete-group-text)
         {:color color-light-red}))

(def delete-group-prompt-text
  (merge (ps-new-group :delete-group-prompt-text)
         {:color          color-gray4}))

(def contact-container
  (merge (ps-new-group :contact-container)
         {:flex-direction  :row
          :justify-content :center
          :align-items     :center}))

(def selected-contact
  {:background-color selected-contact-color})

(def icon-check-container
  (merge (get-in platform-specific [:component-styles :contacts :icon-check])
         {:alignItems     :center
          :justifyContent :center}))

(def toggle-container
  {:width          56
   :height         56
   :alignItems     :center
   :justifyContent :center})

(def check-icon
  {:width  12
   :height 12})

(def delete-group-container
  (merge (ps-new-group :delete-group-container)
         {:height       64
          :padding-top  12}))

(def order-item-container
  {:background-color color-white})

(def order-item-inner-container
  (merge {:flex-direction :row
          :align-items    :center}
         (ps-reorder :order-item-container)))

(def order-item-label
  (ps-reorder :order-item-label))

(def order-item-contacts
  (merge (ps-reorder :order-item-contacts)
         {:padding-left 8
          :color color-gray4}))

(def order-item-icon
  (ps-reorder :order-item-icon))

(def order-item-separator-wrapper
  {:background-color color-white})

(def order-item-separator
  (merge {:height           1
          :background-color color-gray5}
         (ps-reorder :order-item-separator)))

(def toolbar-title-with-count-text
  {:color          text1-color
   :letter-spacing -0.2
   :font-size      17})

(def toolbar-title-with-count-text-count
  (merge toolbar-title-with-count-text
         {:color "#628fe3"}))

(def toolbar-title-with-count
  {:flex-direction :row})

(def toolbar-title-with-count-container
  {:padding-left 6})




