(ns status-im.contexts.preview.quo.wallet.account-card
  (:require
    [quo.components.wallet.account-card.schema :refer [?schema]]
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]
    [status-im.contexts.preview.quo.preview-generator :as preview-gen]
    [utils.collection]))

(def descriptor (preview-gen/schema->descriptor ?schema))

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
