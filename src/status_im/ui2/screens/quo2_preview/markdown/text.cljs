(ns status-im.ui2.screens.quo2-preview.markdown.text
  (:require [quo2.foundations.colors :as colors]
            [status-im.ui2.screens.quo2-preview.preview :as preview]
            [react-native.core :as rn]
            [quo2.components.markdown.text :as quo2]
            [reagent.core :as reagent]))

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
  (let [state (reagent/atom {})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view {:padding-vertical 60}
         [quo2/text @state
          "The quick brown fox jumped over the lazy dog."]]]])))

(defn preview-text []
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
