(ns quo2.screens.messages.new-messages-header
  (:require [quo.react-native :as rn]
            [reagent.core :as reagent]
            [quo.previews.preview :as preview]
            [quo.design-system.colors :as colors]
            [quo2.components.messages.new-messages-header :as new-messages-header]))

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
        [new-messages-header/new-messages-header @state]]])))

(defn preview-new-messages-header []
  [rn/view  {:background-color (:ui-background @colors/theme)
             :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])