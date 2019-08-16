(ns status-im.ui.screens.profile.contact.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def network-info {:background-color :white})

(def profile-info-item
  {:flex-direction :row
   :align-items    :center
   :padding-left   16})

(defn profile-info-text-container [options]
  {:flex          1
   :padding-right (if options 16 40)})

(def profile-info-title
  {:color     colors/gray
   :font-size 14})

(defstyle profile-setting-spacing
  {:ios     {:height 10}
   :android {:height 7}})

(def profile-setting-text
  {:font-size 17})

(def action-container
  {:background-color colors/white})

(def action
  {:background-color colors/blue-transparent-10
   :border-radius    50})

(defn action-label [with-subtext?]
  (cond-> {:color colors/blue}
    with-subtext?
    (assoc :font-size 15
           :line-height 22
           :font-weight "500")))

(def action-subtext
  {:line-height 22
   :font-size   15
   :color colors/gray})

(def action-separator
  {:height           0
   :background-color colors/black-transparent
   :margin-left      50})

(def action-icon-opts
  {:color colors/blue})

(def block-action
  {:background-color colors/red-transparent-10
   :border-radius    50})

(defn block-action-label [with-subtext?]
  {:color colors/red})

(def block-action-icon-opts
  {:color colors/red})

(def profile-setting-text-empty
  (merge profile-setting-text
         {:color colors/gray}))

(def contact-profile-info-container
  {:padding-top      26
   :background-color colors/white})
