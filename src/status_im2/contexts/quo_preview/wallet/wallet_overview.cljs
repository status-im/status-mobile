(ns status-im2.contexts.quo-preview.wallet.wallet-overview
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "State"
    :key     :state
    :type    :select
    :options [{:key   :loading
               :value "Loading"}
              {:key   :default
               :value "Default"}]}
   {:label   "Time frame"
    :key     :time-frame
    :type    :select
    :options [{:key   :none
               :value "None"}
              {:key   :selected
               :value "Selected"}
              {:key   :one-week
               :value "1 Week"}
              {:key   :one-month
               :value "1 Month"}
              {:key   :three-months
               :value "3 Months"}
              {:key   :one-year
               :value "1 Year"}
              {:key   :all-time
               :value "All time"}
              {:key   :custom
               :value "Custom"}]}
   {:label   "Metrics"
    :key     :metrics
    :type    :select
    :options [{:key   :none
               :value "None"}
              {:key   :positive
               :value "Positive"}
              {:key   :negative
               :value "Negative"}]}])

(defn cool-preview
  []
  (let [state (reagent/atom {:state             :default
                             :time-frame        :one-week
                             :metrics           :positive
                             :balance           "€0.00"
                             :date              "20 Nov 2023"
                             :begin-date        "16 May"
                             :end-date          "25 May"
                             :currency-change   "€0.00"
                             :percentage-change "0.00%"})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view {:flex 1}
         [preview/customizer state descriptor]]
        [rn/view
         {:padding-vertical 60
          :flex-direction   :row
          :justify-content  :center}
         [quo/wallet-overview @state]]]])))

(defn preview-wallet-overview
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white
                                           colors/neutral-95)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
