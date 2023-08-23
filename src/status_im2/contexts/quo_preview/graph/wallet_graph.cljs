(ns status-im2.contexts.quo-preview.graph.wallet-graph
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
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
  [{:label   "State:"
    :key     :state
    :type    :select
    :options [{:key   :positive
               :value "Positive"}
              {:key   :negative
               :value "Negative"}]}
   {:label   "Time frame:"
    :key     :time-frame
    :type    :select
    :options [{:key   :empty
               :value "Empty"}
              {:key   :1-week
               :value "1 Week"}
              {:key   :1-month
               :value "1 Month"}
              {:key   :3-months
               :value "3 Months"}
              {:key   :1-year
               :value "1 Year"}
              {:key   :all-time
               :value "All time (500 years data)"}]}])

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

(defn cool-preview
  []
  (let [state (reagent/atom {:state      :positive
                             :time-frame :1-week})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view {:margin-top 300}
         [quo/wallet-graph
          {:data       (generate-data (:time-frame @state))
           :state      (:state @state)
           :time-frame (:time-frame @state)}]]]])))

(defn preview-wallet-graph
  []
  [rn/view
   {:style
    {:background-color (colors/theme-colors
                        colors/white
                        colors/neutral-95)
     :flex             1}}
   [rn/flat-list
    {:style                        {:flex 1}
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str
     :scroll-enabled               false}]])
