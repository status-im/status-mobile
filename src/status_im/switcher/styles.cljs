(ns status-im.switcher.styles
  (:require [quo.theme :as theme]
            [quo2.foundations.colors :as colors]
            [status-im.switcher.constants :as constants]))

(def themes
  {:light {:bottom-tabs-bg-color           colors/neutral-80
           :bottom-tabs-on-scroll-bg-color colors/neutral-80-opa-80
           :bottom-tabs-non-selected-tab   colors/neutral-50
           :bottom-tabs-selected-tab       colors/white
           :switcher-close-button-bg-color colors/white}
   :dark  {:bottom-tabs-bg-color           colors/neutral-80
           :bottom-tabs-on-scroll-bg-color colors/neutral-80-opa-80
           :bottom-tabs-non-selected-tab   colors/neutral-50
           :bottom-tabs-selected-tab       colors/white
           :switcher-close-button-bg-color colors/white}})

(defn get-color [key]
  (get-in themes [(theme/get-theme) key]))

;; Bottom Tabs
(defn bottom-tab-icon [tab-state]
  {:width  24
   :height 24
   :color  (get-color tab-state)})

(defn bottom-tabs [icons-only?]
  {:background-color   (if icons-only? nil (get-color :bottom-tabs-bg-color))
   :flex-direction     :row
   :flex               1
   :justify-content    :space-between
   :height             (constants/bottom-tabs-height)
   :position           :absolute
   :bottom             -1
   :right              0
   :left               0
   :padding-horizontal 16})

;; Switcher
(defn switcher-button []
  {:width      constants/switcher-button-size
   :height     constants/switcher-button-size
   :z-index    2})

(defn merge-switcher-button-common-styles [style]
  (merge
   {:width           constants/switcher-button-size
    :height          constants/switcher-button-size
    :border-radius   constants/switcher-button-radius
    :position        :absolute
    :z-index         2
    :align-items     :center
    :align-self      :center
    :justify-content :center}
   style))

(defn switcher-button-touchable [view-id]
  (merge-switcher-button-common-styles
   {:bottom (constants/switcher-bottom-position view-id)}))

(defn switcher-close-button []
  (merge-switcher-button-common-styles
   {:backgroundColor (get-color :switcher-close-button-bg-color)}))

(defn switcher-screen []
  (dissoc
   (merge-switcher-button-common-styles
    {:background-color colors/switcher-background-opa-80
     :z-index          1
     :overflow         :hidden})
   :justify-content))

(defn switcher-screen-container []
  (let [{:keys [width height]} (constants/dimensions)]
    {:width            width
     :height           (+ height constants/switcher-container-height-padding)
     :align-items      :center
     :position         :absolute}))

(defn switcher-switch-screen []
  {:margin-top  40
   :align-items :center})
