(ns status-im.contexts.preview-screens.quo-preview.wallet.wallet-overview
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :state
    :type    :select
    :options [{:key :loading}
              {:key :default}]}
   {:key     :time-frame
    :type    :select
    :options [{:key :none}
              {:key :selected}
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
              {:key :custom}]}
   {:key     :metrics
    :type    :select
    :options [{:key :none}
              {:key :positive}
              {:key :negative}]}])

(def ^:private networks-list
  [{:source (quo.resources/get-network :ethereum)}
   {:source (quo.resources/get-network :optimism)}
   {:source (quo.resources/get-network :arbitrum)}])

(defn view
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
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-vertical 60
                                    :flex-direction   :row
                                    :justify-content  :center}}
       [quo/wallet-overview
        (assoc @state
               :networks          networks-list
               :dropdown-on-press #(js/alert "On pressed dropdown"))]])))
