(ns status-im2.contexts.quo-preview.wallet.account-card
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.markdown.text :as text]
            [quo2.core :as quo]
            [quo2.components.icon :as icon]
            [reagent.core :as reagent]
            [utils.collection]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Type:"
    :key     :type
    :type    :select
    :options [{:key   :default
               :value "Default"}
              {:key   :watch-only
               :value "Watch Only"}
              {:key   :add-account
               :value "Add Account"}
              {:key   :empty
               :value "Empty"}
             ]}
   {:label   "Customization color:"
    :key     :customization-color
    :type    :select
    :options (map (fn [[color-kw _]]
                    {:key   color-kw
                     :value (name color-kw)})
                  colors/customization)}
   {:label "Name:"
    :key   :name
    :type  :text}
   {:label "Balance:"
    :key   :balance
    :type  :text}
   {:label "Percentage value:"
    :key   :percentage-value
    :type  :text}
   {:label "Amount:"
    :key   :amount
    :type  :text}
   {:label "Metrics:"
    :key   :metrics?
    :type  :boolean}
   {:label "Loading:"
    :key   :loading?
    :type  :boolean}
   {:label "Emoji:"
    :key   :emoji
    :type  :text}])


(defn initial-state
  [type]
  (cond
    (= type :default)
    {:name                "Alisher account"
     :balance             "€2,269.12"
     :percentage-value    "16.9%"
     :amount              "€570.24"
     :customization-color :purple
     :type                :default
     :emoji               "💎"}

    (= type :empty)
    {:name                "Account 1"
     :balance             "€0.00"
     :percentage-value    "0.00"
     :amount              "0.00"
     :customization-color :blue
     :type                :empty
     :emoji               "🔎"}

    (= type :watch-only)
    {:name                "Wach-only"
     :balance             "€2,269.12"
     :percentage-value    "0.00"
     :amount              "0.00"
     :customization-color :blue
     :type                :watch-only
     :emoji               "🔎"}

    (= type :add-account)
    {:customization-color :blue
     :type                :add-account}))


(defn cool-preview
  []
  (let [state (reagent/atom (initial-state :default))]
    (fn []
      (let [type (or (:type @state) :default)]
        (swap! state merge (initial-state type)))
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
       [rn/view {:style {:align-items :center :margin-top 40}}
        [quo/account-card @state]]])))

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
