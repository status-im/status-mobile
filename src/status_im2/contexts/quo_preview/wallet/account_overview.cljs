(ns status-im2.contexts.quo-preview.wallet.account-overview
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Time frame string"
    :key   :time-frame-string
    :type  :text}
   {:label "Time frame to string"
    :key   :time-frame-to-string
    :type  :text}
   {:label "Percentage change"
    :key   :percentage-change
    :type  :text}
   {:label "Currency change"
    :key   :currency-change
    :type  :text}
   {:label "Current value"
    :key   :current-value
    :type  :text}
   {:label   "State"
    :key     :state
    :type    :select
    :options [{:key   :default
               :value "Default"}
              {:key   :loading
               :value "Loading"}]}
   {:label   "Metrics"
    :key     :metrics
    :type    :select
    :options [{:key   "Positive"
               :value :positive}
              {:key   "Negative"
               :value :negative}]}
   {:label "Account name"
    :key   :account-name
    :type  :text}
   {:label   "Customization color:"
    :key     :customization-color
    :type    :select
    :options (map (fn [color]
                    (let [k (get color :name)]
                      {:key k :value k}))
                  (quo/picker-colors))}
   {:label   "Account"
    :key     :account
    :type    :select
    :options [{:key   :watched-address
               :value "Watched address"}
              {:key   :default
               :value "Default"}]}
   {:label   "Time frame"
    :key     :time-frame
    :type    :select
    :options [{:key   :one-week
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
               :value "Custom"}]}])

(defn preview-account-overview
  []
  (let [state (reagent/atom {:metrics              :positive
                             :currency-change      "€0.00"
                             :percentage-change    "0.00%"
                             :current-value        "€0.00"
                             :account-name         "Diamond Hand"
                             :time-frame           :custom
                             :time-frame-string    "16 May"
                             :time-frame-to-string "25 May"
                             :account              :default
                             :customization-color  :blue})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view {:style {:padding-bottom 150}}
        [rn/view
         {:style {:padding-vertical 60
                  :flex-direction   :row
                  :justify-content  :center}}
         [quo/account-overview @state]]]])))
