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

(defn home-token-icon-style [idx]
  {:align-items    :center
   :shadow-radius  16
   :shadow-opacity 1
   :shadow-color   (:shadow-01 @colors/theme)
   :shadow-offset  {:width 0 :height 4}
   :width          20
   :height         20
   :border-radius  20
   :position       :absolute
   :top            0
   :left           (* idx 10)})

(defn home-tokens-icons [c]
  {:height            20
   :margin-horizontal 6
   :width             (- (* 20 c) (* 10 (dec c)))})

(defn invite-instructions []
  {:border-top-width    1
   :border-top-color    (:ui-01 @colors/theme)
   :border-bottom-width 1
   :border-bottom-color (:ui-01 @colors/theme)
   :padding-top         (:small spacing/spacing)})

(defn invite-instructions-title []
  (merge
   (:tiny spacing/padding-vertical)
   (:base spacing/padding-horizontal)))

(defn invite-instructions-content []
  (merge (:tiny spacing/padding-vertical)
         (:base spacing/padding-horizontal)))

(defn invite-warning []
  (merge
   (:tiny spacing/padding-vertical)
   (:base spacing/padding-horizontal)
   {:background-color    (:warning-02 @colors/theme)
    :border-top-width    1
    :border-top-color    (:warning-01 @colors/theme)
    :border-bottom-width 1
    :border-bottom-color (:warning-01 @colors/theme)}))

(defn modal-token-icon-style [idx]
  {:align-items    :center
   :shadow-radius  16
   :shadow-opacity 1
   :shadow-color   (:shadow-01 @colors/theme)
   :shadow-offset  {:width 0 :height 4}
   :width          40
   :height         40
   :border-radius  20
   :position       :absolute
   :top            0
   :left           (* idx 20)})

(defn modal-tokens-icons-style [c]
  {:height         40
   :width          (- (* 40 c) (* 20 (dec c)))})

(defn modal-perks-container []
  {:border-radius      8
   :border-width       1
   :border-color       (:ui-02 @colors/theme)
   :width              "100%"
   :margin-vertical    8
   :padding-vertical   8
   :padding-horizontal 12})
