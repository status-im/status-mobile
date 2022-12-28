(ns quo2.components.settings.accounts.style
  (:require [quo2.foundations.colors :as colors]))

(def card {:position      :relative
           :padding       8
           :border-radius 16
           :height        160
           :width         160})

(defn background-top [custom-color]
  {:position                :absolute
   :top                     0
   :border-top-left-radius  16
   :border-top-right-radius 16
   :width                   160
   :height                  64
   :background-color        (-> colors/customization
                                (get-in [custom-color 50])
                                (colors/alpha 0.2))})

(defn background-bottom []
  {:position         :absolute
   :top              40
   :border-radius    16
   :width            160
   :height           120
   :background-color (colors/theme-colors
                      colors/white
                      colors/neutral-90)})

(defn avatar-border []
  {:margin          2
   :justify-content :center
   :align-items     :center
   :width           52
   :height          52
   :border-radius   14
   :border-width    2
   :border-color    (colors/theme-colors
                     colors/white
                     colors/neutral-90)})

(def menu-button-container {:justify-content :center
                            :align-items     :center
                            :align-self      :flex-start})
(defn menu-button-color []
  {:background-color (colors/theme-colors
                      colors/white-opa-40
                      colors/neutral-80-opa-40)})

(defn address-text []
  {:color (colors/theme-colors
           colors/neutral-50
           colors/neutral-40)})

(def card-top {:flex-direction  :row
               :align-items     :center
               :justify-content :space-between})

(def card-bottom {:flex              1
                  :margin-horizontal 4
                  :margin-top        4})
