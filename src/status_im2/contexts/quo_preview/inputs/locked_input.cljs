(ns status-im2.contexts.quo-preview.inputs.locked-input
  (:require [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "show icon"
    :key   :icon
    :type  :boolean}
   {:label "label"
    :key   :label
    :type  :text}
   {:label "Value"
    :key   :value
    :type  :text}])

(defn preview-locked-input
  []
  (let [state (reagent/atom {:value "$1,648.34"
                             :label "Network fee"})]
    (fn []
      (let [{:keys [label value icon]} @state]
        [rn/view
         {:background-color (colors/theme-colors colors/white colors/neutral-90)
          :flex             1
          :flex-direction   :column
          :padding-top      20
          :padding-left     20}
         [rn/view {:style {:height 200}}
          [preview/customizer state descriptor]]
         [rn/view
          {:style {:margin-top     11
                   :flex-direction :row}}
          [quo/locked-input
           {:icon            (when icon :i/gas)
            :label           label
            :container-style {:margin-right      20
                              :margin-horizontal 20
                              :flex              1}}
           value]]]))))
