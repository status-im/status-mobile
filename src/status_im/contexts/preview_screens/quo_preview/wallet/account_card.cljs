(ns status-im.contexts.preview-screens.quo-preview.wallet.account-card
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]
    [utils.collection]))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :default}
              {:key :watch-only}
              {:key :add-account}
              {:key :empty}
              {:key :missing-keypair}]}
   (preview/customization-color-option)
   {:key :name :type :text}
   {:key :balance :type :text}
   {:key :percentage-value :type :text}
   {:key :amount :type :text}
   {:key :metrics? :type :boolean}
   {:key :loading? :type :boolean}
   {:key :emoji :type :text}])

(defn initial-state
  [type]
  (case type
    :default
    {:name                "Alisher account"
     :balance             "€2,269.12"
     :percentage-value    "16.9%"
     :amount              "€570.24"
     :customization-color :turquoise
     :metrics?            true
     :type                :default
     :emoji               "💎"}

    :empty
    {:name                "Account 1"
     :balance             "€0.00"
     :percentage-value    "€0.00"
     :customization-color :blue
     :metrics?            true
     :type                :empty
     :emoji               "🍑"}

    :watch-only
    {:name                "Ben’s fortune"
     :balance             "€2,269.12"
     :percentage-value    "16.9%"
     :amount              "€570.24"
     :metrics?            true
     :type                :watch-only
     :customization-color :army
     :emoji               "💸"}

    :missing-keypair
    {:name                "Trip to Vegas"
     :balance             "€2,269.12"
     :percentage-value    "16.9%"
     :amount              "€570.24"
     :metrics?            true
     :customization-color :turquoise
     :type                :missing-keypair
     :emoji               "🎲"}

    :add-account
    {:customization-color :blue
     :on-press            #(js/alert "Button pressed")
     :metrics?            true
     :type                :add-account}))


(defn view
  []
  (let [state (reagent/atom (initial-state :default))]
    [:f>
     (fn []
       (rn/use-effect
        (fn [] (reset! state (initial-state (:type @state))))
        [(:type @state)])
       [preview/preview-container
        {:state      state
         :descriptor descriptor}
        [quo/account-card @state]])]))
