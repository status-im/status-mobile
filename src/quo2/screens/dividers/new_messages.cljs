(ns quo2.screens.dividers.new-messages
  (:require [quo.react-native :as rn]
            [reagent.core :as reagent]
            [quo.previews.preview :as preview]
            [quo2.foundations.colors :as colors]
            [quo2.components.dividers.new-messages :as new-messages]))

(def descriptor [{:label   "Label"
                  :key     :label
                  :type    :text}
                 {:label   "Color"
                  :key     :color
                  :type    :select
                  :options [{:key   :primary
                             :value "Primary"}
                            {:key   :purple
                             :value "Purple"}
                            {:key   :indigo
                             :value "Indigo"}
                            {:key   :turquoise
                             :value "Turquoise"}
                            {:key   :blue
                             :value "Blue"}
                            {:key   :green
                             :value "Green"}
                            {:key   :yellow
                             :value "yellow"}
                            {:key   :orange
                             :value "Orange"}
                            {:key   :red
                             :value "Red"}
                            {:key   :pink
                             :value "Pink"}
                            {:key   :brown
                             :value "Brown"}
                            {:key   :beige
                             :value "Beige"}]}])
(defn cool-preview []
  (let [state     (reagent/atom {:label "New messages"
                                 :color :primary})]
    (fn []
      [rn/view {:margin-bottom 50
                :padding       16}
       [preview/customizer state descriptor]
       [rn/view {:padding-vertical 60}
        [new-messages/new-messages @state]]])))

(defn preview-new-messages []
  [rn/view  {:background-color (colors/theme-colors
                                colors/white
                                colors/neutral-90)
             :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])