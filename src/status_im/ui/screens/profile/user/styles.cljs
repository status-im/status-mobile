(ns status-im.ui.screens.profile.user.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.profile.components.styles :as profile.components.styles]
            [status-im.ui.components.styles :as components.styles]))

(def share-contact-code
  {:margin-horizontal 16
   :flex-direction    :row
   :justify-content   :space-between
   :align-items       :center
   :height            42
   :border-radius     components.styles/border-radius
   :background-color  (colors/alpha colors/blue 0.1)})

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

(def qr-code
  {:background-color colors/gray-lighter})

(def qr-code-viewer
  {:flex-grow      1
   :flex-direction :column})

(defstyle logout-text
  (merge profile.components.styles/settings-item-text
         {:color colors/red}))

(defstyle my-profile-info-container
  {:background-color colors/white})

(def advanced-button
  {:margin-top    16
   :margin-bottom 12})

(def advanced-button-container
  {:align-items     :center
   :justify-content :center})

(def advanced-button-container-background
  {:padding-left     16
   :padding-right    12
   :padding-vertical 6
   :border-radius    18
   :background-color (colors/alpha colors/blue 0.1)})

(def advanced-button-row
  {:flex-direction :row
   :align-items    :center})

(def advanced-button-label
  {:font-size      15
   :letter-spacing -0.2
   :color          colors/blue})