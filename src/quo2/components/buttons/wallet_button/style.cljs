(ns quo2.components.buttons.wallet-button.style
  (:require [quo2.foundations.colors :as colors]))

(defn get-border-color
  [{:keys [pressed? theme]}]
  (if (= pressed? true)
    (colors/theme-colors colors/neutral-40 colors/neutral-60 theme)
    (colors/theme-colors colors/neutral-30 colors/neutral-70 theme)))

(defn main
  [{:keys [pressed? theme disabled?]}]
  {:border-color    (get-border-color {:pressed? pressed?
                                       :theme    theme})
   :border-width    1
   :border-radius   10
   :width           32
   :height          32
   :justify-content :center
   :align-items     :center
   :opacity         (when disabled? 0.3)})
