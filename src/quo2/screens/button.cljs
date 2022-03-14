(ns quo2.screens.button
  (:require [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [quo.design-system.colors :as colors]
            [quo2.components.button :as quo2]))

(def descriptor [{:label   "Type:"
                  :key     :type
                  :type    :select
                  :options [{:key   :primary
                             :value "Primary"}
                            {:key   :secondary
                             :value "Secondary"}
                            {:key   :grey
                             :value "Grey"}
                            {:key   :outline
                             :value "Outline"}
                            {:key   :ghost
                             :value "Ghost"}
                            {:key   :danger
                             :value "Danger"}]}
                 {:label   "Size:"
                  :key     :size
                  :type    :select
                  :options [{:key   56
                             :value "56"}
                            {:key   40
                             :value "40"}
                            {:key   32
                             :value "32"}
                            {:key   24
                             :value "24"}]}
                 {:label "Icon:"
                  :key   :icon
                  :type  :boolean}
                 {:label "Above icon:"
                  :key   :above
                  :type  :boolean}
                 {:label "After icon:"
                  :key   :after
                  :type  :boolean}
                 {:label "Before icon:"
                  :key   :before
                  :type  :boolean}
                 {:label "Disabled:"
                  :key   :disabled
                  :type  :boolean}
                 {:label "Label"
                  :key   :label
                  :type  :text}])

(defn cool-preview []
  (let [state  (reagent/atom {:label "Press Me"
                              :size  40})
        label  (reagent/cursor state [:label])
        before (reagent/cursor state [:before])
        after  (reagent/cursor state [:after])
        above  (reagent/cursor state [:above])
        icon  (reagent/cursor state [:icon])]
    (fn []
      [rn/view {:margin-bottom 50
                :padding       16}
       [rn/view {:flex 1}
        [preview/customizer state descriptor]]
       [rn/view {:padding-vertical 60
                 :flex-direction   :row
                 :justify-content  :center}
        [quo2/button (merge (dissoc @state
                                    :theme :before :after)
                            {:on-press #(println "Hello world!")}
                            (when @above
                              {:above :main-icons2/placeholder})
                            (when @before
                              {:before :main-icons2/placeholder})
                            (when @after
                              {:after :main-icons2/placeholder}))
         (if @icon :main-icons2/placeholder @label)]]])))

(defn preview-button []
  [rn/view {:background-color (:ui-background @colors/theme)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])