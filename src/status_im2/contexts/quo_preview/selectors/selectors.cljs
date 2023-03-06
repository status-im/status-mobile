(ns status-im2.contexts.quo-preview.selectors.selectors
  (:require ["react-native" :refer [StyleSheet]]
            [oops.core :refer [oget]]
            [quo.previews.preview :as preview]
            [quo.react-native :as rn]
            [quo2.components.markdown.text :as text]
            [quo2.components.selectors.selectors :as quo2]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [status-im.ui.components.react :as react]))

(def descriptor
  [{:label "Disabled?"
    :key   :disabled?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:disabled false})]
    (fn []
      [rn/view
       {:margin-bottom 50
        :padding       16}
       [preview/customizer state descriptor]

       [rn/view
        {:padding-vertical 60
         :align-items      :center}
        [text/text {:size :heading-2} "Toggle"]
        [quo2/toggle
         {:container-style {:margin-top 0}
          :disabled?       (:disabled? @state)}]
        [text/text {:size :heading-2} "Radio"]
        [quo2/radio
         {:container-style {:margin-top 0}
          :disabled?       (:disabled? @state)}]
        [text/text {:size :heading-2} "Checkbox"]
        [quo2/checkbox
         {:container-style {:margin-top 0}
          :disabled?       (:disabled? @state)}]
        [text/text {:size :heading-2} "Checkbox Prefill"]
        [quo2/checkbox-prefill
         {:container-style {:margin-top 0}
          :disabled?       (:disabled? @state)}]]

       [rn/view
        {:padding-vertical 60
         :align-items      :center}
        [react/blur-view
         {:style      (oget StyleSheet "absoluteFill")
          :blurAmount 20
          :blurType   (if (colors/dark?) :dark :light)}
         [react/linear-gradient
          {:style  (oget StyleSheet "absoluteFill")
           :colors [(colors/alpha "#4CB4EF" 0.2)
                    (colors/alpha "#FB8F61" 0.2)
                    (colors/alpha "#647084" 0.2)]
           :start  {:x 0 :y 0}
           :end    {:x 1 :y 1}}]]

        [text/text {:size :heading-2} "Toggle"]
        [quo2/toggle
         {:container-style     {:margin-top 0}
          :disabled?           (:disabled? @state)
          :blurred-background? true}]
        [text/text {:size :heading-2} "Radio"]
        [quo2/radio
         {:container-style     {:margin-top 0}
          :disabled?           (:disabled? @state)
          :blurred-background? true}]
        [text/text {:size :heading-2} "Checkbox"]
        [quo2/checkbox
         {:container-style     {:margin-top 0}
          :disabled?           (:disabled? @state)
          :blurred-background? true}]
        [text/text {:size :heading-2} "Checkbox Prefill"]
        [quo2/checkbox-prefill
         {:container-style     {:margin-top 0}
          :disabled?           (:disabled? @state)
          :blurred-background? true}]]])))

(defn preview-selectors
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                      1
     :keyboardShouldPersistTaps :always
     :header                    [cool-preview]
     :key-fn                    str}]])
