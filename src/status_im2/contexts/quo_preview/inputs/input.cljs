(ns status-im2.contexts.quo-preview.inputs.input
  (:require [clojure.string :as string]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Type:"
    :key     :type
    :type    :select
    :options [{:key   :text
               :value "Text"}
              {:key   :password
               :value "Password"}]}
   {:label "Blur:"
    :key   :blur?
    :type  :boolean}
   {:label   "Override Theme:"
    :key     :override-theme
    :type    :select
    :options [{:key   :dark
               :value "Dark"}
              {:key   :light
               :value "Light"}]}
   {:label "Error:"
    :key   :error?
    :type  :boolean}
   {:label "Icon:"
    :key   :icon-name
    :type  :boolean}
   {:label "Disabled:"
    :key   :disabled?
    :type  :boolean}
   {:label "Clearable:"
    :key   :clearable?
    :type  :boolean}
   {:label "Small:"
    :key   :small?
    :type  :boolean}
   {:label "Multiline:"
    :key   :multiline
    :type  :boolean}
   {:label "Button:"
    :key   :button
    :type  :boolean}
   {:label "Label:"
    :key   :label
    :type  :text}
   {:label   "Char limit:"
    :key     :char-limit
    :type    :select
    :options [{:key   10
               :value "10"}
              {:key   50
               :value "50"}
              {:key   100
               :value "100"}]}
   {:label "Value:"
    :key   :value
    :type  :text}])

(defn cool-preview
  []
  (let [state (reagent/atom {:type                :text
                             :blur                false
                             :override-theme      nil
                             :placeholder         "Type something"
                             :error               false
                             :icon-name           false
                             :value               ""
                             :clearable           false
                             :on-char-limit-reach #(js/alert
                                                    (str "Char limit reached: " %))})]
    (fn []
      (let [blank-label? (string/blank? (:label @state))
            icon?        (boolean (:icon-name @state))
            button-props {:on-press #(js/alert "Button pressed!")
                          :text     "My button"}]
        [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
         [rn/view {:style {:padding-bottom 150}}
          [rn/view {:style {:flex 1}}
           [preview/customizer state descriptor]]
          [preview/blur-view
           {:style                 {:flex            1
                                    :align-items     :center
                                    :margin-vertical 20}
            :show-blur-background? (:blur? @state)}
           [rn/view {:style {:width 300}}
            [quo/input
             (cond-> @state
               :always          (assoc
                                 :on-clear?      #(swap! state assoc :value "")
                                 :on-change-text #(swap! state assoc :value %))
               (:button @state) (assoc :button button-props)
               blank-label?     (dissoc :label)
               icon?            (assoc :icon-name :i/placeholder))]]]]]))))

(defn preview-input
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}}
   [rn/flat-list
    {:style                        {:flex 1}
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
