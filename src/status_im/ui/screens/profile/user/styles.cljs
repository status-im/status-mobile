(ns status-im.ui.screens.profile.user.styles
  (:require [quo2.foundations.colors :as colors]
            [react-native.safe-area :as safe-area]))

(def share-link-button
  {:margin-top        12
   :margin-horizontal 16
   :margin-bottom     16})

(def radius 16)

(def top-background-view {:background-color colors/magenta-opa-40
                          :position :absolute
                          :top 0
                          :left 0
                          :height 400
                          :width 400
                          :z-index -1})

(def toolbar {:padding-bottom 16
              :padding-top (safe-area/get-top)
              :flex-direction :row
              :align-items :center
              :justify-content :space-between})

(def header-icon-style {:border-radius 10
                        :margin-left 16
                        :background-color colors/white-opa-10})

(def right-accessories {:flex-direction :row
                        :align-items :center
                        :justify-content :space-between
                        :margin-right 16})

(def avatar {:margin-top 16
             :margin-horizontal 16
             :z-index 100})

(def user-info {:background-color colors/neutral-95
                :padding-horizontal 16
                :border-top-left-radius 20
                :border-top-right-radius 20
                :margin-top -32
                :padding-top 32
                :padding-bottom 16})

(def container-style {:background-color colors/neutral-95})

(def items-container {:background-color colors/neutral-95})

(def rounded-view {:margin-top 16
                   :margin-horizontal 16
                   :overflow "hidden"
                   :border-radius radius})

(def logout-container {:align-items   :center
                       :justify-content :center
                       :flex-direction :row
                       :height 54
                       :margin-horizontal 16
                       :margin-top 16
                       :overflow "hidden"
                       :border-radius radius
                       :background-color colors/danger-50-opa-20})

