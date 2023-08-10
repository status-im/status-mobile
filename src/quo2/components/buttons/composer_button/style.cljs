(ns quo2.components.buttons.composer-button.style
  (:require [quo2.foundations.colors :as colors]))

(defn get-border-color
  [{:keys [pressed? blur? theme]}]
  (cond
    (and pressed? blur?) (colors/theme-colors colors/neutral-80-opa-20 colors/white-opa-20 theme)
    (= pressed? true)    (colors/theme-colors colors/neutral-40 colors/neutral-60 theme)
    (= blur? true)       (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10 theme)
    :else                (colors/theme-colors colors/neutral-30 colors/neutral-70 theme)))

(defn get-label-color
  [{:keys [blur? theme]}]
  (cond
    blur? (colors/theme-colors colors/neutral-80-opa-70 colors/white-opa-70 theme)
    :else (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)))

(defn main
  [{:keys [pressed? blur? theme disabled?]}]
  {:border-color    (get-border-color {:pressed? pressed?
                                       :blur?    blur?
                                       :theme    theme})
   :border-width    1
   :border-radius   10
   :width           32
   :height          32
   :justify-content :center
   :align-items     :center
   :opacity         (when disabled? 0.3)})
