(ns status-im2.contexts.quo-preview.graph.interactive-graph
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [quo2.components.graph.utils :as utils]
            [goog.string :as gstring]))

(def weekly-data
  [{:value 123
    :date  "Sun"}
   {:value 160
    :date  "Mon"}
   {:value 435
    :date  "Tue"}
   {:value 2345
    :date  "Wed"}
   {:value 1444
    :date  "Thu"}
   {:value 931
    :date  "Fri"}
   {:value 1200
    :date  "Sat"}])

(defn generate-crypto-token-prices
  [num-elements volatility]
  (loop [n             num-elements
         prices        []
         prev-price    (rand-int 100000)
         volatility    volatility
         current-day   (rand-int 31) ; Start with a random day
         months        ["Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"]
         current-month (rand-nth months)] ; Start with a random month
    (if (zero? n)
      (vec (reverse prices))
      (let [fluctuation  (* prev-price volatility)
            random-delta (- (rand fluctuation) (/ fluctuation 2))
            new-price    (max 1 (+ prev-price random-delta))
            new-day      (if (= current-day 1) 31 (dec current-day)) ; Decrease the day
            new-month    (if (= current-day 1)
                           (let [prev-month-index (dec (.indexOf months current-month))]
                             (if (>= prev-month-index 0)
                               (nth months prev-month-index)
                               (nth months (dec (count months)))))
                           current-month)
            new-prices   (conj prices
                               {:value new-price
                                :date  (str new-day " " new-month)})]
        (recur (dec n) new-prices new-price volatility new-day months new-month)))))


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
               :value "All time (500 years data)"}]}
   {:label "Reference value:"
    :key   :reference-value
    :type  :number}
   {:label "Reference prefix:"
    :key   :reference-prefix
    :type  :text}
   {:label   "Reference decimal separator:"
    :key     :decimal-separator
    :type    :select
    :options [{:key   :dot
               :value "Dot (.)"}
              {:key   :comma
               :value "Comma (,)"}]}
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
    (if (= time-frame :1-week)
      weekly-data
      (generate-crypto-token-prices data-points volatility))))

(defn f-view
  [state]
  (fn []
    (rn/use-effect (fn []
                     (let [time-frame    (:time-frame @state)
                           data          (generate-data time-frame)
                           highest-value (utils/find-highest-value data)
                           lowest-value  (utils/find-lowest-value data)
                           average-value (gstring/format "%.2f" (/ (+ highest-value lowest-value) 2))]
                       (swap! state assoc :data data :reference-value average-value)))
                   [(:time-frame @state)])
    [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
     [rn/view {:padding-bottom 150}
      [preview/customizer state descriptor]
      [quo/interactive-graph
       {:data                (:data @state)
        :state               (:state @state)
        :reference-value     (:reference-value @state)
        :reference-prefix    (:reference-prefix @state)
        :customization-color (:customization-color @state)
        :decimal-separator   (:decimal-separator @state)}]]]))

(defn view
  []
  (let [data          (generate-data :1-week)
        highest-value (utils/find-highest-value data)
        lowest-value  (utils/find-lowest-value data)
        average-value (gstring/format "%.2f" (/ (+ highest-value lowest-value) 2))
        state         (reagent/atom {:state               :positive
                                     :time-frame          :1-week
                                     :customization-color :blue
                                     :reference-value     average-value
                                     :reference-prefix    "$"
                                     :decimal-separator   :dot
                                     :data                data})]
    [rn/scroll-view
     {:style
      {:background-color (colors/theme-colors
                          colors/white
                          colors/neutral-95)
       :flex             1}}
     [:f> f-view state]]))
