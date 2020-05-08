(ns status-im.ui.components.invite.style
  (:require [quo.design-system.spacing :as spacing]
            [quo.design-system.colors :as colors]))

(def reward-item-title (merge (:base spacing/padding-horizontal)
                              {:padding-top    12
                               :padding-bottom 4}))
(defn reward-item-content []
  (merge (:base spacing/padding-horizontal)
         (:base spacing/padding-vertical)
         {:background-color    (:interactive-02 @colors/theme)
          :flex-direction      :row
          :border-bottom-width 1
          :border-top-width    1
          :border-color        (:border-02 @colors/theme)}))

(defn reward-token-icon [idx]
  {:align-items    :center
   :shadow-radius  16
   :shadow-opacity 1
   :shadow-color   (:shadow-01 @colors/theme)
   :shadow-offset  {:width 0 :height 4}
   :width          40
   :height         40
   :border-radius  20
   :position       :absolute
   :top            (* idx 20)})

(def reward-description {:flex 1})

(defn reward-tokens-icons [c]
  {:margin-right 16
   :width        40
   :height       (- (* 40 c) (* 20 (dec c)))})
