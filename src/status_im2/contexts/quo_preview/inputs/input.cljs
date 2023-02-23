(ns status-im2.contexts.quo-preview.inputs.input
  (:require [quo2.components.input.view :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Type:"
    :key     :type
    :type    :select
    :options [{:key   :password
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
   ;;
   {:label "Error:"
    :key   :error
    :type  :boolean}
   ;;

   {:label   "Icon:"
    :key     :icon-name
    :type    :select
    :options [{:key   :i/placeholder
               :value "Placeholder icon"}
              {:key   :i/email
               :value "Email icon"}]}
   ]
  )

(defn cool-preview
  []
  (let [state (reagent/atom {:type        :password
                             :variant     :dark
                             :placeholder "Type something"
                             :error       false
                             :icon-name   :i/placeholder})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:style {:padding-bottom 150}}
        [rn/view {:style {:flex 1}}
         [preview/customizer state descriptor]]
        [rn/view
         {:style {:flex             1
                  :align-items      :center
                  :padding-vertical 60
                  :background-color (case (:variant @state)
                                      :dark-blur colors/neutral-80-blur-opa-80
                                      :dark colors/neutral-95
                                      :light-blur "#deef"
                                      :white)}}
         [rn/view {:style {:width 288}}
          [quo2/input @state]]]]])))

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
