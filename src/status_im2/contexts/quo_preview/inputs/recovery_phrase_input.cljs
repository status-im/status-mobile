(ns status-im2.contexts.quo-preview.inputs.recovery-phrase-input
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Text"
    :key   :text
    :type  :text}
   {:label "Placeholder"
    :key   :placeholder
    :type  :text}
   {:label "Blur"
    :key   :blur?
    :type  :boolean}
   {:label "Mark errors"
    :key   :mark-errors?
    :type  :boolean}
   {:label   "Customization color"
    :key     :customization-color
    :type    :select
    :options (map (fn [[color _]]
                    {:key color :value (name color)})
                  colors/customization)}
   {:label   "Word limit"
    :key     :word-limit
    :type    :select
    :options [{:key nil :value "No limit"}
              {:key 5 :value "5 words"}
              {:key 10 :value "10 words"}
              {:key 20 :value "20 words"}]}])

(defn cool-preview
  []
  (let [state (reagent/atom {:text                ""
                             :placeholder         "Type or paste your recovery phrase"
                             :customization-color :blue
                             :word-limit          20
                             :mark-errors?        true})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:style {:padding-bottom 150}}
        [rn/view {:style {:flex 1}}
         [preview/customizer state descriptor]
         [quo/text {:size :paragraph-2}
          "(Any word with at least 6 chars is marked as error)"]]
        [preview/blur-view
         {:style                 {:align-items     :center
                                  :margin-vertical 20
                                  :width           "100%"}
          :height                200
          :show-blur-background? (:blur? @state)}
         [rn/view
          {:style {:height 150
                   :width  "100%"}}
          [quo/recovery-phrase-input
           {:mark-errors?        (:mark-errors? @state)
            :error-pred          #(> (count %) 5)
            :on-change-text      #(swap! state assoc :text %)
            :placeholder         (:placeholder @state)
            :customization-color (:customization-color @state)
            :word-limit          (:word-limit @state)}
           (:text @state)]]]]])))

(defn preview-recovery-phrase-input
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white colors/neutral-95)
            :flex             1}}
   [rn/flat-list
    {:style                     {:flex 1}
     :keyboardShouldPersistTaps :always
     :header                    [cool-preview]
     :key-fn                    str}]])
