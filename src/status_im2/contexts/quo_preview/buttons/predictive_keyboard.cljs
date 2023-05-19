(ns status-im2.contexts.quo-preview.buttons.predictive-keyboard
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Type"
    :key     :type
    :type    :select
    :options [{:key :error :value "Error"}
              {:key :empty :value "Empty"}
              {:key :info :value "Info"}
              {:key :words :value "Words"}]}
   {:label "Text"
    :key   :text
    :type  :text}
   {:label "Blur"
    :key   :blur?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:type :info :text "Enter 12, 18 or 24 words separated by a space"})
        blur? (reagent/cursor state [:blur?])]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         (when @blur?
           [blur/webview-blur
            {:style         {:position         :absolute
                             :left             0
                             :right            0
                             :top              0
                             :bottom           0
                             :background-color colors/neutral-80-opa-70}
             :overlay-color :transparent}])
         [rn/view {:padding-vertical 60 :align-items :center}
          [quo/predictive-keyboard
           (merge @state
                  {:words    ["label" "label" "labor" "ladder" "lady" "lake"]
                   :on-press #(js/alert (str "Pressed: " %))})]]]]])))

(defn preview-predictive-keyboard
  []
  [rn/view
   {:background-color (colors/theme-colors
                       colors/white
                       colors/neutral-95)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
