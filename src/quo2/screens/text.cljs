(ns quo2.screens.text
  (:require [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [reagent.core :as reagent]
            [quo2.components.text :as quo2]
            [quo.design-system.colors :as colors]))

(def descriptor [{:label   "Size:"
                  :key     :size
                  :type    :select
                  :options [{:key   :heading-1
                             :value "Heading 1"}
                            {:key   :heading-2
                             :value "Heading 2"}
                            {:key   :paragraph-1
                             :value "Paragraph 1"}
                            {:key   :paragraph-2
                             :value "Paragraph 2"}
                            {:key   :label
                             :value "Label"}]}
                 {:label   "Weight:"
                  :key     :weight
                  :type    :select
                  :options [{:key   :regular
                             :value "Regular"}
                            {:key   :medium
                             :value "Medium"}
                            {:key   :semi-bold
                             :value "Semi-bold"}
                            {:key   :monospace
                             :value "Monospace"}]}])

(defn cool-preview []
  (let [state     (reagent/atom {})]
    (fn []
      [rn/view {:margin-bottom 50
                :padding       16}
       [preview/customizer state descriptor]
       [rn/view {:padding-vertical 60}
        [quo2/text @state
         "The quick brown fox jumped over the lazy dog."]]])))

(defn preview-text []
  [rn/view {:background-color (:ui-background @colors/theme)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])