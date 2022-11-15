(ns status-im.ui2.screens.quo2-preview.selectors.selectors
  (:require [react-native.core :as rn]
            [status-im.ui2.screens.quo2-preview.preview :as preview]
            [reagent.core :as reagent]
            [quo2.components.selectors.selectors :as quo2]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]))

(def descriptor [{:label "Disabled?"
                  :key   :disabled?
                  :type  :boolean}])

(defn cool-preview []
  (let [state (reagent/atom {:disabled false})]
    (fn []
      [rn/view {:margin-bottom 50
                :padding       16}
       [preview/customizer state descriptor]
       [rn/view {:padding-vertical 60
                 :align-items      :center}
        [text/text {:size :heading-2} "Toggle"]
        [quo2/toggle {:container-style {:margin-top 0}
                      :disabled? (:disabled? @state)}]
        [text/text {:size :heading-2} "Radio"]
        [quo2/radio {:container-style {:margin-top 0}
                     :disabled? (:disabled? @state)}]
        [text/text {:size :heading-2} "Checkbox"]
        [quo2/checkbox {:container-style {:margin-top 0}
                        :disabled? (:disabled? @state)}]
        [text/text {:size :heading-2} "Checkbox Prefill"]
        [quo2/checkbox-prefill {:container-style {:margin-top 0}
                                :disabled? (:disabled? @state)}]]])))

(defn preview-selectors []
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
