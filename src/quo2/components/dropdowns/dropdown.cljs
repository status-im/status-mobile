(ns quo2.components.dropdowns.dropdown
  (:require [quo2.components.dropdowns.old-button-view :as old-button]))

(defn dropdown
  [_ _]
  (fn [{:keys [on-change selected] :as opts} children]
    ;TODO: Dropdown needs to be implemented/refactored to be its own component - https://github.com/status-im/status-mobile/issues/16456
    [old-button/button
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
