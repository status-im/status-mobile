(ns status-im.switcher.styles
  (:require [quo2.foundations.colors :as colors]
            [status-im.utils.platform :as platform]
            [status-im.switcher.constants :as constants]))

;; Bottom Tabs
(defn bottom-tabs-container [pass-through?]
  {:background-color    (if pass-through? colors/neutral-100-opa-70 colors/neutral-100)
   :flex                1
   :align-items         :center
   :flex-direction      :column
   :height              (constants/bottom-tabs-container-height)
   :position            :absolute
   :bottom              -1
   :right               0
   :left                0
   :accessibility-label :bottom-tabs-container})

(defn bottom-tabs []
  {:flex-direction      :row
   :position            :absolute
   :bottom              (if platform/android? 8 34)
   :flex                1
   :accessibility-label :bottom-tabs})

;; Home Stack
(defn home-stack []
  (let [{:keys [width height]} (constants/dimensions)]
    {:border-bottom-left-radius  20
     :border-bottom-right-radius 20
     :background-color           (colors/theme-colors colors/neutral-5 colors/neutral-95)
     :overflow                   :hidden
     :position                   :absolute
     :width                      width
     :height                     (- height (constants/bottom-tabs-container-height))}))
