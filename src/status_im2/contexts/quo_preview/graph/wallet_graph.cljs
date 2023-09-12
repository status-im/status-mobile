(ns status-im2.contexts.quo-preview.graph.wallet-graph
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(defn generate-crypto-token-prices
  [num-elements volatility]
  (loop [n          num-elements
         prices     []
         prev-price (rand-int 100000)
         volatility volatility]
    (if (zero? n)
      (vec (reverse prices))
      (let [fluctuation  (* prev-price volatility)
            random-delta (- (rand fluctuation) (/ fluctuation 2))
            new-price    (max 1 (+ prev-price random-delta))
            new-prices   (conj prices {:value new-price})]
        (recur (dec n) new-prices new-price volatility)))))

(def descriptor
  [{:key     :state
    :type    :select
    :options [{:key :positive}
              {:key :negative}]}
   {:key     :time-frame
    :type    :select
    :options [{:key :empty}
              {:key :1-week}
              {:key :1-month}
              {:key :3-months}
              {:key :1-year}
              {:key   :all-time
               :value "All time (500 years data)"}]}
   (preview/customization-color-option)])

(defn generate-data
  [time-frame]
  (let [data-points (case time-frame
                      :empty    0
                      :1-week   7
                      :1-month  30
                      :3-months 90
                      :1-year   365
                      (* 365 500))
        volatility  (case time-frame
                      :empty    0
                      :1-week   2
                      :1-month  1
                      :3-months 0.5
                      :1-year   0.05
                      0.005)]
    (generate-crypto-token-prices data-points volatility)))

(defn view
  []
  (let [state (reagent/atom {:state      :positive
                             :time-frame :1-week})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-horizontal 0 :margin-top 200}}
       [quo/wallet-graph
        {:data                (generate-data (:time-frame @state))
         :state               (:state @state)
         :time-frame          (:time-frame @state)
         :customization-color (:customization-color @state)}]])))
