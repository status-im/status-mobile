(ns quo.previews.button
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [quo.previews.preview :as preview]
            [quo.react-native :as rn]
            [reagent.core :as reagent]))

(def descriptor
  [{:label   "Type:"
    :key     :type
    :type    :select
    :options [{:key   :primary
               :value "Primary"}
              {:key   :secondary
               :value "Secondary"}
              {:key   :icon
               :value "Icon"}]}
   {:label   "Theme:"
    :key     :theme
    :type    :select
    :options [{:key   :main
               :value "Main"}
              {:key   :negative
               :value "Negative"}
              {:key   :positive
               :value "Positive"}
              {:key   :accent
               :value "Accent"}]}
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

(defn cool-preview
  []
  (let [state  (reagent/atom {:label "Press Me"
                              :type  :primary
                              :theme :main
                              :icon  :main-icons/share})
        theme  (reagent/cursor state [:theme])
        label  (reagent/cursor state [:label])
        before (reagent/cursor state [:before])
        after  (reagent/cursor state [:after])]
    (fn []
      [rn/view
       {:margin-bottom 50
        :padding       16}
       [rn/view {:flex 1}
        [preview/customizer state descriptor]]
       [rn/view
        {:padding-vertical 16
         :flex-direction   :row
         :justify-content  :center}
        [quo/button
         (merge (dissoc @state
                 :theme
                 :before
                 :after)
                {:on-press #(println "Hello world!")}
                (when @theme
                  {:theme @theme})
                (when @before
                  {:before :main-icons/back})
                (when @after
                  {:after :main-icons/next}))
         @label]]])))

(defn preview-button
  []
  [rn/view
   {:background-color (:ui-background @colors/theme)
    :flex             1}
   [rn/flat-list
    {:flex                      1
     :keyboardShouldPersistTaps :always
     :header                    [cool-preview]
     :key-fn                    str}]])
