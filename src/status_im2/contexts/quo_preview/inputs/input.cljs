(ns status-im2.contexts.quo-preview.inputs.input
  (:require
    [clojure.string :as string]
    [quo2.components.input.view :as quo2]
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
   {:label   "Variant:"
    :key     :variant
    :type    :select
    :options [{:key   :light
               :value "Light"}
              {:key   :dark
               :value "Dark"}
              {:key   :light-blur
               :value "Light blur"}
              {:key   :dark-blur
               :value "Dark blur"}]}
   {:label "Error:"
    :key   :error
    :type  :boolean}
   {:label "Icon:"
    :key   :icon-name
    :type  :boolean}
   {:label "Disabled:"
    :key   :disabled
    :type  :boolean}
   {:label "Clearable:"
    :key   :clearable
    :type  :boolean}
   {:label "Small:"
    :key   :small
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
                             :variant             :light-blur
                             :placeholder         "Type something"
                             :error               false
                             :icon-name           false
                             :value               ""
                             :clearable           false
                             :on-char-limit-reach #(js/alert
                                                    (str "Char limit reached: " %))})]
    (fn []
      (let [background-color (case (:variant @state)
                               :dark-blur  "rgb(39, 61, 81)"
                               :dark       colors/neutral-95
                               :light-blur "rgb(233,247,247)"
                               :white)
            blank-label?     (string/blank? (:label @state))
            icon?            (boolean (:icon-name @state))
            button-props     {:on-press #(js/alert "Button pressed!")
                              :text     "My button"}]
        [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
         [rn/view {:style {:padding-bottom 150}}
          [rn/view {:style {:flex 1}}
           [preview/customizer state descriptor]]
          [rn/view
           {:style {:flex             1
                    :align-items      :center
                    :padding-vertical 60
                    :background-color background-color}}
           [rn/view {:style {:width 300}}
            [quo2/input
             (cond-> @state
               :always          (assoc
                                 :on-clear       #(swap! state assoc :value "")
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
    {:style                     {:flex 1}
     :keyboardShouldPersistTaps :always
     :header                    [cool-preview]
     :key-fn                    str}]])
