(ns status-im.ui.screens.profile.contact.styles
  (:require [status-im.ui.components.colors :as colors]))

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

(def block-action-label
  {:color colors/red
   :padding-top      26
   :margin-left      16})

(def contact-profile-details-container
  {:padding-top      26})

(def contact-profile-detail-share-icon
  {:color colors/gray-transparent-40})
