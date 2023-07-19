(ns quo2.components.numbered-keyboard.keyboard-key.style
  (:require [quo2.foundations.colors :as colors]))

(defn get-label-color
  [disabled? theme blur?]
  (if disabled?
    (if (or (= :dark theme) blur?)
      colors/white-opa-30
      colors/neutral-30)
    (if (or (= :dark theme) blur?)
      colors/white
      colors/neutral-100)))

(defn toggle-background-color
  [pressed-in? blur? theme]
  (if pressed-in?
    (cond
      blur?            colors/white-opa-10
      (= :light theme) colors/neutral-10
      (= :dark theme)  colors/neutral-80)
    :transparent))

(defn container
  [color]
  {:width            48
   :height           48
   :justify-content  :center
   :align-items      :center
   :border-radius    999
   :background-color color})
