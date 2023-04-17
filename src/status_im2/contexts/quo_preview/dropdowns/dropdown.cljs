(ns status-im2.contexts.quo-preview.dropdowns.dropdown
  (:require [quo2.components.dropdowns.dropdown :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Icon"
    :key     :icon
    :type    :select
    :options [{:key   :main-icons/placeholder
               :value "Placeholder"}
              {:key   :main-icons/locked
               :value "Wallet"}]}
   {:label "Disabled"
    :key   :disabled?
    :type  :boolean}
   {:label "Default item"
    :key   :default-item
    :type  :text}
   {:label "Use border?"
    :key   :use-border?
    :type  :boolean}
   {:label   "Border color"
    :key     :border-color
    :type    :select
    :options (map
              (fn [c]
                {:key   c
                 :value c})
              (keys colors/customization))}
   {:label "DD color"
    :key   :dd-color
    :type  :text}
   {:label   "Size"
    :key     :size
    :type    :select
    :options [{:key   :big
               :value "big"}
              {:key   :medium
               :value "medium"}
              {:key   :small
               :value "small"}]}])

(defn cool-preview
  []
  (let [items         ["Banana"
                       "Apple"
                       "COVID +18"
                       "Orange"
                       "Kryptonite"
                       "BMW"
                       "Meh"]
        state         (reagent/atom {:icon         :main-icons/placeholder
                                     :default-item "item1"
                                     :use-border?  false
                                     :dd-color     (colors/custom-color :purple 50)
                                     :size         :big})
        selected-item (reagent/cursor state [:default-item])
        on-select     #(reset! selected-item %)]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         {:padding-vertical 60
          :align-items      :center}
         [rn/text (str "Selected item: " @selected-item)]
         [quo2/dropdown
          (merge @state
                 {:on-select on-select
                  :items     items})]]]])))

(defn preview-dropdown
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :flex-grow                    1
     :nestedScrollEnabled          true
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
