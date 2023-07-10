(ns quo2.components.dropdowns.dropdown
  (:require [quo2.components.buttons.button.view :as button]))

(defn dropdown
  [_ _]
  (fn [{:keys [on-change selected] :as opts} children]
    [button/button
     (merge
      opts
      {:after                   (if selected :i/chevron-top :i/chevron-down)
       :icon-size               12
       :icon-container-size     14
       :icon-container-rounded? true
       :override-after-margins  {:left  7
                                 :right 9}
       :pressed                 selected
       :on-press                #(when on-change (on-change selected))})
     children]))
