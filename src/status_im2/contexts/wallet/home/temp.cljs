(ns status-im2.contexts.wallet.home.temp
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [utils.re-frame :as rf]))

(defn wallet-temporary-navigation
  []
  [rn/view
   {:style {:flex            1
            :align-items     :center
            :justify-content :center}}
   [quo/text {} "TEMPORARY NAVIGATION"]
   [quo/button {:on-press #(rf/dispatch [:navigate-to :wallet-accounts])}
    "Navigate to Account"]
   [quo/button {:on-press #(rf/dispatch [:navigate-to :wallet-create-account])}
    "Create Account"]
   [quo/button {:on-press #(rf/dispatch [:navigate-to :wallet-saved-addresses])}
    "Saved Addresses"]
   [quo/button {:on-press #(rf/dispatch [:navigate-to :wallet-collectibles])}
    "Collectibles"]])

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

(def graph-state
  {:state      :positive
   :time-frame :1-week})
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

(def wallet-graph-state
  {:data       (generate-data (:time-frame graph-state))
   :state      (:state graph-state)
   :time-frame (:time-frame graph-state)})

(def wallet-overview-state
  {:state             :default
   :time-frame        :none
   :metrics           :none
   :balance           "€0.00"
   :date              "20 Nov 2023"
   :begin-date        "16 May"
   :end-date          "25 May"
   :currency-change   "€0.00"
   :percentage-change "0.00%"})

(def account-cards
  [{:name                "Account 1"
    :balance             "€0.00"
    :percentage-value    "€0.00"
    :customization-color :blue
    :type                :empty
    :emoji               "🍑"}
   {:customization-color :blue
    :on-press            #(js/alert "Button pressed")
    :type                :add-account}])

(def tokens
  [{:token               :snt
    :state               :default
    :status              :empty
    :customization-color :blue
    :values              {:crypto-value      "0.00"
                          :fiat-value        "€0.00"
                          :percentage-change "0.00"
                          :fiat-change       "€0.00"}}
   {:token               :eth
    :state               :default
    :status              :empty
    :customization-color :blue
    :values              {:crypto-value      "0.00"
                          :fiat-value        "€0.00"
                          :percentage-change "0.00"
                          :fiat-change       "€0.00"}}
   {:token               :dai
    :state               :default
    :status              :empty
    :customization-color :blue
    :values              {:crypto-value      "0.00"
                          :fiat-value        "€0.00"
                          :percentage-change "0.00"
                          :fiat-change       "€0.00"}}])
