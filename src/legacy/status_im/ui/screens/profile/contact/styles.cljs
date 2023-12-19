(ns legacy.status-im.ui.screens.profile.contact.styles
  (:require
    [legacy.status-im.ui.components.colors :as colors]))

(def block-action-label
  {:color         colors/red
   :margin-top    26
   :margin-bottom 50
   :margin-left   16})

(def contact-profile-details-container
  {:padding-top 26})

(def contact-profile-detail-share-icon
  {:color colors/gray-transparent-40})

(defn updates-descr-cont
  []
  {:border-width               1
   :border-color               colors/gray-lighter
   :border-top-right-radius    16
   :border-bottom-left-radius  16
   :border-top-left-radius     16
   :border-bottom-right-radius 4
   :padding-horizontal         12
   :padding-vertical           6})
