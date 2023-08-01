(ns status-im2.contexts.quo-preview.numbered-keyboard.numbered-keyboard
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Blur:"
    :key   :blur?
    :type  :boolean}
   {:label "Disable:"
    :key   :disabled?
    :type  :boolean}
   {:label "Delete Key:"
    :key   :delete-key?
    :type  :boolean}
   {:label   "Left Action:"
    :type    :select
    :key     :left-action
    :options [{:key   :dot
               :value "Dot"}
              {:key   :face-id
               :value "Face ID"}
              {:key   :none
               :value "None"}]}])

(defn cool-preview
  []
  (let [state (reagent/atom {:disabled?   false
                             :on-press    (fn [item] (js/alert (str item " pressed")))
                             :blur?       false
                             :delete-key? true
                             :left-action :dot})]
    (fn []
      [rn/view
       [rn/view {:style {:flex 1}}
        [preview/customizer state descriptor]]
       [preview/blur-view
        {:style                 {:flex              1
                                 :align-self        :center
                                 :justify-self      :center
                                 :margin-horizontal 20}
         :show-blur-background? (:blur? @state)
         :height                270
         :blur-view-props       (when (:blur? @state)
                                  {:overlay-color colors/neutral-80-opa-80})}
        [quo/numbered-keyboard @state]]])))

(defn preview-numbered-keyboard
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}}
   [rn/flat-list
    {:style                        {:flex 1}
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
