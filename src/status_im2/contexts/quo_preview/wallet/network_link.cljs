(ns status-im2.contexts.quo-preview.wallet.network-link
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]))


(def descriptor
  [{:label   "Shape:"
    :key     :shape
    :type    :select
    :options [{:key   :linear
               :value "Linear"}
              {:key   :1x
               :value "1x"}
              {:key   :2x
               :value "2x"}]}])

(defn preview
  []
  (let [state (reagent/atom {:shape :linear})]
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
        [quo/network-link (merge @state {:preview? true})]]])))
