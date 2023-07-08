(ns status-im2.contexts.quo-preview.wallet.account-card
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.markdown.text :as text]
            [quo2.core :as quo]
            [quo2.components.icon :as icon]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
  ))

(def mock-data
  [{:id                  1
    :name                "Alisher account"
    :balance             "€2,269.12"
    :percentage-value    "16.9%"
    :amount              "€570.24"
    :customization-color :army
    :type                :default
    :emoji               "💎"}
   {:id               2
    :name             "Ben’s fortune"
    :balance          "€2,269.12"
    :percentage-value "16.9%"
    :amount           "€570.24"
    :watch-only?      true
    :type             :watch-only
    :emoji            "💸"}
   {:id                  3
    :type                :add-account
    :customization-color :blue
    :handler             #(js/alert "Add account pressed")}])

(def descriptor
  [{:label   "Type:"
    :key     :type
    :type    :select
    :options [{:key   :default
               :value "Default"}
              {:key   :watch-only
               :value "Watch Only"}
              {:key   :add-account
               :value "Add Account"}]}])

(defn cool-preview
  []
  (let [state (reagent/atom {:type :default})]
    (fn []
      [rn/view
       {:style {:flex 1}}
       [rn/view
        {:style {:margin-vertical 40
                 :padding-left    40
                 :flex-direction  :row
                 :align-items     :center}}
        [text/text
         {:size   :heading-1
          :weight :semi-bold} "Account card"]
        [rn/view
         {:style {:width            20
                  :height           20
                  :border-radius    60
                  :background-color colors/success-50
                  :align-items      :center
                  :justify-content  :center
                  :margin-left      8}}
         [icon/icon :i/check {:color colors/white :size 16}]]]
       [rn/view {:style {:flex 1}}
        [preview/customizer state descriptor]]
       (let [selected-type (:type @state)
             filtered-data (filter #(= selected-type (:type %)) mock-data)]
         (for [data filtered-data]
           [rn/view {:style {:align-items :center :margin-vertical 40}}
            [quo/account-card data]]))])))


(defn preview-account-card
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white
                                           colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
