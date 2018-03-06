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

(defstyle version
          {:padding-horizontal 16
           :font-size          11
           :text-color         "#93ab91"
           :android            {:font-family "Roboto-Regular"}
           :ios                {:font-family "SFUIText-Regular"}})

(defstyle logout-text
  (merge profile.components.styles/settings-item-text
         {:color colors/red}))