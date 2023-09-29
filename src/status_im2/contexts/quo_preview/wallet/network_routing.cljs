(ns status-im2.contexts.quo-preview.wallet.network-routing
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Number of networks"
    :key     :number-networks
    :type    :select
    :options [{:key 2} {:key 3} {:key 4} {:key 5}]}])

(defn- fake-call-to-get-amounts
  [{:keys [new-amount fixed-index current-values on-success]}]
  (let [number-networks        (count current-values)
        amount-difference      (- (get current-values fixed-index) new-amount)
        difference-distributed (/ amount-difference (dec number-networks))
        new-values             (assoc (mapv #(+ % difference-distributed) current-values)
                                      fixed-index
                                      new-amount)]
    (js/setTimeout #(on-success new-values) (rand-nth (range 700 5000 250)))))

(defn preview-internal
  [{:keys [total-amount number-networks] :as descriptor-state}]
  (let [initial-amount   (/ total-amount number-networks)
        networks         (reagent/atom
                          [{:amount     initial-amount
                            :max-amount (descriptor-state :max-amount-0)
                            :color      "#758EEB"}
                           {:amount     initial-amount
                            :max-amount (descriptor-state :max-amount-1)
                            :color      "#E76E6E"}
                           {:amount     initial-amount
                            :max-amount (descriptor-state :max-amount-2)
                            :color      "#6BD5F0"}
                           {:amount     initial-amount
                            :max-amount (descriptor-state :max-amount-3)
                            :color      "#50AF4D"}
                           {:amount     initial-amount
                            :max-amount (descriptor-state :max-amount-4)
                            :color      "#DCBA27"}])
        requesting-data? (reagent/atom false)]
    (fn []
      (let [asked-networks (vec (take number-networks @networks))
            on-success-fn  (fn [new-network-amounts]
                             (reset! requesting-data? false)
                             (swap! networks
                               #(map (fn [network new-amount]
                                       (assoc network :amount new-amount))
                                     %
                                     new-network-amounts)))]
        [rn/view
         [quo/network-routing
          {:total-amount       total-amount
           :networks           asked-networks
           :requesting-data?   @requesting-data?
           :on-amount-selected (fn [new-amount selected-idx]
                                 (reset! requesting-data? true)
                                 (fake-call-to-get-amounts
                                  {:new-amount     new-amount
                                   :fixed-index    selected-idx
                                   :current-values (mapv :amount asked-networks)
                                   :on-success     on-success-fn}))}]
         (reduce (fn [acc {:keys [amount max-amount color]}]
                   (conj acc
                         [rn/view
                          {:style {:flex-direction  :row
                                   :margin-vertical 12}}
                          [rn/view
                           {:style {:background-color color
                                    :width            24
                                    :height           24
                                    :margin-right     12}}]
                          [quo/text
                           "Max limit: " max-amount " Amount: " (subs (str amount) 0 6)]]))
                 [rn/view {:style {:margin-vertical 12}}
                  [quo/text "Total amount: " (reduce + (map :amount asked-networks))]]
                 asked-networks)]))))

(defn preview
  []
  (let [descriptor-state (reagent/atom {:total-amount    400
                                        :number-networks 4
                                        :max-amount-0    350
                                        :max-amount-1    350
                                        :max-amount-2    300
                                        :max-amount-3    250
                                        :max-amount-4    200})]
    (fn []
      [preview/preview-container {:state descriptor-state :descriptor descriptor}
       [rn/view {:style {:flex 1 :margin-vertical 28}}
        ^{:key (str "preview--network-routing-" (:number-networks @descriptor-state))}
        [preview-internal @descriptor-state]]])))
