(ns status-im.contexts.preview.quo.wallet.amount-input
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key  :max-value
    :type :number}
   {:key  :min-value
    :type :number}
   {:key  :value
    :type :number}
   {:type    :select
    :key     :status
    :options [{:key :default}
              {:key :error}]}])

(defn view
  []
  (let [state        (reagent/atom {:max-value 10000
                                    :min-value 0
                                    :value     1
                                    :status    :default})
        on-inc-press (fn [] (swap! state #(assoc % :value (inc (:value %)))))
        on-dec-press (fn [] (swap! state #(assoc % :value (dec (:value %)))))]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [quo/amount-input
        (merge
         @state
         {:on-dec-press on-dec-press
          :on-inc-press on-inc-press})]])))
