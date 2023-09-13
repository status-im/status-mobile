(ns quo2.components.navigation.page-nav.style
  (:require [quo2.foundations.colors :as colors]))

(defn container
  [margin-top]
  {:margin-top         margin-top
   :padding-horizontal 20
   :padding-vertical   12
   :height             56
   :flex-direction     :row
   :justify-content    :space-between
   :align-items        :center})

(defn center-content-container
  [centered?]
  {:flex              1
   :margin-horizontal 12
   :flex-direction    :row
   :align-items       :center
   :justify-content   (if centered? :center :flex-start)})

(def account-switcher-placeholder
  {:width            32
   :height           32
   :border-radius    10
   :background-color (colors/custom-color :purple 50)})

(def right-actions-container
  {:flex-direction :row})

(def right-actions-spacing
  {:width 12})

(def right-content-min-size
  {:min-width 32 :min-height 32})

(def token-logo
  {:width 16 :height 16})

(def token-name
  {:margin-left         4
   :text-align-vertical :center})

(defn token-abbreviation
  [theme background]
  (let [color (case background
                :photo nil
                :blur  (colors/theme-colors colors/neutral-80-opa-70 colors/white-opa-40 theme)
                (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))]
    {:margin-left         4
     :color               color
     :text-align-vertical :center}))

(def channel-emoji
  {:width 20 :height 20})

(defn channel-icon-color
  [theme background]
  (case background
    :photo nil
    :blur  (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-40 theme)
    (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)))

(def channel-name
  {:margin-horizontal   4
   :text-align-vertical :center})

(def group-avatar-picture
  {:margin-right 8})

(def title-description-container
  {:height          32
   :justify-content :center})

(def title-description-title
  {:text-align-vertical :center})

(defn title-description-description
  [theme background]
  (let [color (case background
                :photo (colors/theme-colors colors/neutral-80-opa-80-blur colors/white-opa-70 theme)
                :blur  (colors/theme-colors colors/neutral-80-opa-50 colors/white-opa-40 theme)
                (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))]
    {:color               color
     :text-align-vertical :center}))

(def community-network-logo
  {:width        24
   :height       24
   :margin-right 6})
