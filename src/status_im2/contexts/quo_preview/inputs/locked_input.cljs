(ns status-im2.contexts.quo-preview.inputs.locked-input
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key :icon :type :boolean}
   {:key :label :type :text}
   {:key :value :type :text}])

(defn view
  []
  (let [state (reagent/atom {:value "$1,648.34"
                             :label "Network fee"})]
    (fn []
      (let [{:keys [label value icon]} @state]
        [preview/preview-container {:state state :descriptor descriptor}
         [quo/locked-input
          {:icon            (when icon :i/gas)
           :label           label
           :container-style {:margin-right      20
                             :margin-horizontal 20
                             :flex              1}}
          value]]))))
