(ns status-im2.contexts.quo-preview.list-items.account
  (:require
    [quo2.core :as quo]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :default} {:key :balance-positive} {:key :balance-neutral} {:key :balance-negative}
              {:key :tag} {:key :action}]}
   {:key :selectable? :type :boolean}
   {:key :title-icon? :type :boolean}
   {:key  :emoji
    :type :text}
   (preview/customization-color-option {:key :account-color})
   {:key :blur? :type :boolean}
   (preview/customization-color-option)])

(defn view
  []
  (let [state (reagent/atom {:type                :default
                             :selectable?         false
                             :title-icon?         false
                             :customization-color :blue
                             :account-color       :purple
                             :emoji               "🍑"
                             :title               "New House"
                             :address             "0x21a...49e"
                             :balance-props       {:crypto-value      "0.00"
                                                   :fiat-value        "€0.00"
                                                   :percentage-change "0.0"
                                                   :fiat-change       "€0.00"}
                             :token-props         {:symbol "SNT"
                                                   :value  "1,000"}
                             :on-options-press    #(js/alert "Options button pressed!")})]
    (fn [] [preview/preview-container
            {:state                 state
             :descriptor            descriptor
             :blur?                 (:blur? @state)
             :show-blur-background? true
             :blur-dark-only?       true}
            [quo/account-item
             (merge @state
                    {:account-props {:name                (:title @state)
                                     :address             (:address @state)
                                     :emoji               (:emoji @state)
                                     :customization-color (:account-color @state)}})]])))
