(ns status-im2.contexts.quo-preview.wallet.progress-bar
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "State:"
    :key     :state
    :type    :select
    :options [{:key   :pending
               :value "pending"}
              {:key   :confirmed
               :value "confirmed"}
              {:key   :finalized
               :value "finalized"}
              {:key   :error
               :value "error"}]}
   (preview/customization-color-option)])

(defn preview
  []
  (let [state (reagent/atom {:state               :pending
                             :customization-color :blue})]
    (fn []
      [rn/view
       {:style {:flex               1
                :padding-horizontal 20}}
       [rn/view {:style {:min-height 150}}
        [preview/customizer state descriptor]]
       [rn/view
        {:style {:flex        1
                 :padding-top 40
                 :align-items :center}}
        [quo/progress-bar @state]]])))
