(ns status-im.ui.screens.syncing.sheets.sync-device-notice.styles
  (:require [clojure.string :as string]
            [quo.design-system.colors :as colors]))

(def sync-devices-header
  {:width :100%})

(def sync-devices-header-image
  {:width :100%
   :height 192})

(def sync-devices-body-container
  {:margin-bottom 20
   :border-radius 20
   :z-index 2
   :margin-top -16
   :background-color colors/white
   :padding 20})

(def header-text
  {:font-size 27
   :font-weight :600})

(def instructions-text
  {:font-size 15
   :font-weight :400
   :margin-top 8})

(def list-item-text
  {:font-size 13
   :font-weight :400
   :margin-top 18})

(def setup-syncing-button
  {:margin-top 21})

(def secondary-body-container
  {:margin-top 21})
