(ns status-im.ui.components.drawer.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.styles :as common]))

(def drawer
  {:flex             1
   :background-color common/color-white
   :justify-content  :space-between})

;; profile

(def upper-container
  {:margin     16
   :margin-top 0})

(def profile-container
  {:padding          16
   :border-radius    8
   :background-color common/color-light-blue4})

(def user-photo-container
  {:height 52
   :width  52})

(def name-input-wrapper
  {:margin-top 24
   :padding    0
   :height     20})

(defnstyle name-input-text [valid?]
  {:line-height 24
   :height      20
   :padding     0
   :color       (if valid? common/color-black common/color-red)
   :android     {:font-size 16}
   :ios         {:font-size 17}})

(def status-container
  {:flex-direction :row
   :margin-top     3})

(defstyle status-input-view
  {:min-height          67
   :width               236
   :font-size           15
   :line-height         21
   :padding-left        0
   :padding-top         5
   :padding-bottom      0
   :margin-bottom       0
   :text-align-vertical :top
   :color               common/color-black})

(defnstyle status-view [placeholder?]
  (-> (dissoc status-input-view :color)
      (cond-> placeholder?
        (assoc :color common/color-gray))
      (merge
       {:min-height 0
        :ios        {:padding-top 10}})))

(def options-button
  {:position :absolute
   :top      16
   :right    16})

;; network

(def network-label-container
  {:margin-top 16})

(defstyle network-label
  {:color   common/color-gray4
   :android {:font-size 12}
   :ios     {:font-size 14}})

(def network-title
  {:color     common/color-gray6
   :font-size 16})

;; transactions

(def transactions-list-separator
  {:margin-left 48})

(def empty-transactions-title-container
  {:align-items :center})

(defstyle transactions-title-container
  {:margin-left 16
   :android     {:margin-bottom 16}
   :ios         {:margin-bottom 8}})

(defstyle transactions-title
  {:color   common/color-gray4
   :android {:font-size 12}
   :ios     {:font-size 14}})

(def transaction
  {:padding        16
   :flex-direction :row})

(defstyle transaction-icon
  {:width        24
   :height       24
   :margin-right 8
   :android      {:margin-top 2}})

(def transaction-info
  {:width 180})

(def transaction-value-container
  {:flex-direction :row})

(def transaction-value
  {:font-size   20
   :line-height 22
   :color       common/color-black})

(def transaction-unit
  {:font-size   20
   :line-height 22
   :color       common/color-gray
   :margin-left 8})

(def transaction-details-container
  {:margin-top     8
   :flex-direction :row})

(defstyle transaction-to
  {:line-height 16
   :color       common/color-gray4
   :android     {:font-size 12}
   :ios         {:font-size 14}})

(defstyle transaction-recipient
  {:line-height 16
   :margin-left 4
   :color       common/color-black
   :flex-shrink 1
   :android     {:font-size 12}
   :ios         {:font-size 14}})

(defstyle transaction-time
  {:line-height 16
   :color       common/color-gray4
   :margin-left 8
   :android     {:font-size 12}
   :ios         {:font-size 14}})

(def transaction-picture
  {:margin-top  3
   :margin-left 16
   :min-width   40
   :min-height  40})

(def view-all-transactions-button
  {:height           52
   :justify-content  :center
   :align-items      :center
   :border-top-width 1
   :border-top-color common/color-light-gray2})

(defstyle view-all-transactions-text
  {:color   common/color-light-blue
   :android {:font-size 14}
   :ios     {:font-size 17}})

(def switch-account-container
  {:align-items   :center
   :margin-bottom 10
   :margin-top    10})

(defstyle switch-account-text
  {:color   common/color-light-blue
   :android {:font-size 14}
   :ios     {:font-size 17}})
