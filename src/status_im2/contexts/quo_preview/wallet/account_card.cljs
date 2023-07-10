(ns status-im2.contexts.quo-preview.wallet.account-card
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.markdown.text :as text]
            [quo2.core :as quo]
            [quo2.components.icon :as icon]
            [reagent.core :as reagent]
            [utils.collection]
            [status-im2.contexts.quo-preview.preview :as preview]
  ))

(def mock-data
  [{:id                  1
    :name                "Trip to Vegas"
    :balance             "€21,872.93"
    :percentage-value    "16.9%"
    :amount              "€570.24"
    :customization-color :turquoise
    :type                :default
    :emoji               "🎲"}
   {:id               2
    :name             "Ben’s fortune"
    :balance          "€2,269.12"
    :percentage-value "16.9%"
    :amount           "€570.24"
    :watch-only?      true
    :type             :watch-only
    :emoji            "💸"}
   {:id                  3
    :name                "Alisher account"
    :balance             "€2,269.12"
    :percentage-value    "16.9%"
    :amount              "€570.24"
    :customization-color :purple
    :type                :default
    :emoji               "💎"}
   {:id                  4
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
               :value "Add Account"}]}
   {:label "Show FlatList:"
    :key   :show-flatlist
    :type  :boolean}
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
   {:label "Emoji:"
    :key   :emoji
    :type  :text}])

(defn- separator
  []
  [rn/view {:style {:width 12}}])


(defn cool-preview
  []
  (let [state (reagent/atom {:name                "Alisher account"
                             :balance             "€2,269.12"
                             :percentage-value    "16.9%"
                             :amount              "€570.24"
                             :customization-color (if (= :type :add-account)
                              :blue
                              :army)
                             :type                :default
                             :emoji               "💎"})]
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
       (if (:show-flatlist @state)
         [rn/view {:style {:margin-top 40 :margin-vertical 20}}
          [rn/flat-list
           {:data                              mock-data
            :key-extractor                     #(-> % :id)
            :horizontal                        true
            :content-container-style           {:padding-horizontal 20}
            :content-container-styles          (fn [index]
                                                 (let [last-index (- (count mock-data) 1)]
                                                   (if (= index last-index)
                                                     {:flex-grow 1 :align-self :flex-start}
                                                     {})))
            :separator                         [separator]
            :render-fn                         quo/account-card
            :shows-horizontal-scroll-indicator false}]]
         (let [selected-type (:type @state)
               filtered-data (->> mock-data
                                  (filter #(= selected-type (:type %)))
                                  (utils.collection/distinct-by :type))]
           (for [_ filtered-data]
             [rn/view {:style {:align-items :center :margin-top 40}}
              [quo/account-card (assoc @state :type selected-type)]])))])))


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
