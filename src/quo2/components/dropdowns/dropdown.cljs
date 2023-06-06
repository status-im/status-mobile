(ns quo2.components.dropdowns.dropdown
  (:require [quo2.components.buttons.button :as button]))

(defn dropdown
  [_ _]
  (fn [{:keys [on-change selected] :as opts} children]
    [button/button
     (merge
      opts
      {:after                   (if selected :i/pullup :i/dropdown)
       :icon-secondary-no-color true
       :pressed                 selected
       :on-press                #(when on-change (on-change selected))})
     children]))
