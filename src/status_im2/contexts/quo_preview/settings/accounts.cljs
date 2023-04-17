(ns status-im2.contexts.quo-preview.settings.accounts
  (:require [clojure.string :as string]
            [quo2.components.settings.accounts.view :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Custom color"
    :key     :customization-color
    :type    :select
    :options (map (fn [[k _]]
                    {:key k :value (string/capitalize (name k))})
                  colors/customization)}
   {:label "Account name"
    :key   :account-name
    :type  :text}
   {:label "Account address"
    :key   :account-address
    :type  :text}])

(defn cool-preview
  []
  (let [state (reagent/atom {:customization-color :blue
                             :account-name        "Booze for Dubai"
                             :account-address     "0x21a ... 49e"
                             :avatar-icon         :i/placeholder
                             :on-press-menu       (fn []
                                                    (js/alert "Menu button pressed"))})]
    (fn []
      [rn/view
       {:margin-bottom 50
        :padding       16}
       [preview/customizer state descriptor]
       [rn/view
        {:padding-vertical 100
         :align-items      :center
         :background-color (colors/theme-colors
                            colors/neutral-30
                            colors/neutral-95)}
        [quo/account @state]]])))

(defn preview-accounts
  []
  [rn/view {:style {:flex 1}}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
