(ns status-im.contexts.preview-screens.quo-preview.wallet.account-overview
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key :time-frame-string :type :text}
   {:key :time-frame-to-string :type :text}
   {:key :percentage-change :type :text}
   {:key :currency-change :type :text}
   {:key :current-value :type :text}
   {:key     :state
    :type    :select
    :options [{:key :default}
              {:key :loading}]}
   {:key     :metrics
    :type    :select
    :options [{:key :positive}
              {:key :negative}]}
   {:key :account-name :type :text}
   (preview/customization-color-option)
   {:key     :account
    :type    :select
    :options [{:key :watched-address}
              {:key :default}]}
   {:key     :time-frame
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

(defn view
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
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-vertical 60
                                    :flex-direction   :row
                                    :justify-content  :center}}
       [quo/account-overview @state]])))
