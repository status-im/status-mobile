(ns status-im2.contexts.quo-preview.wallet.transaction-progress
  (:require [quo2.components.wallet.transaction-progress.view :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]))

(def mockData
  [{:title    "Title"
    :process [{:status   "Pending"
               :progress "0/4"
               :network  "Mainnet"}]}
   {:title    "Title"
    :process [{:status   "Pending"
               :progress "0/4"
               :network  "Mainnet"}
              {:status   "Pending"
               :progress "0/4"
               :network  "Arbitrum"}]}
   {:title    "Title"
    :process [{:status   "Pending"
               :progress "2/4"
               :network  "Mainnet"}]}
   {:title    "Title"
    :process [{:status   "Confirmed"
               :progress "4/4"
               :network  "Mainnet"}]}
   {:title    "Title"
    :process [{:status   "Confirmed"
               :progress "0/1"
               :network  "Mainnet"}
              {:status   "Confirmed"
               :progress "0/1"
               :network  "Arbitrum"}]}
   {:title    "Title"
    :process [{:status   "Finalised"
               :progress "Epoch 181,329"
               :network  "Mainnet"}]} 
   {:title    "Title"
    :process [{:status   "Finalised"
               :progress "Epoch 181,329"
               :network  "Mainnet"}
              {:status   "Finalised"
               :progress "Epoch 181,329"
               :network  "Arbitrum"}]}
   {:title    "Title"
    :process [{:status   "Failed"
               :progress "0/1"
               :network  "Mainnet"}]} 
   {:title    "Title"
    :process [{:status   "Failed"
               :progress "0/1"
               :network  "Mainnet"}
              {:status   "Failed"
               :progress "0/1"
               :network  "Arbitrum"}]}])

(defn cool-preview
  []
  (let [_ (reagent/atom {:top-value               10
                             :bottom-value            10
                             :top-value-bg-color      colors/neutral-100
                             :top-value-text-color    colors/white
                             :bottom-value-bg-color   colors/neutral-100
                             :bottom-value-text-color colors/white})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 15}
        [rn/view
         {:padding-vertical 1
          :flex-direction   :row
          :justify-content  :center}
         ]]])))

(defn render-transaction-progress-item
  [props]
   [rn/view
   {:margin-bottom 10
    :flex-direction   :row
    :justify-content  :center}
          [quo2/transaction_progress_card props]
   ])

(defn preview-transaction-progress 
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :data                         mockData
     :key-fn                       str 
     :render-fn                   render-transaction-progress-item}]])
