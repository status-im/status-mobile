(ns quo.components.buttons.logout-button.style
  (:require
    [quo.foundations.colors :as colors]))

(defn main
  [{:keys [pressed? disabled?]}]
  {:background-color (if pressed? colors/danger-50-opa-40 colors/danger-50-opa-30)
   :border-radius    12
   :height           40
   :gap              4
   :padding-right    3
   :flex-direction   :row
   :justify-content  :center
   :align-items      :center
   :opacity          (when disabled? 0.2)})
