(ns status-im2.contexts.quo-preview.text-combinations.preview
  (:require [quo2.components.text-combinations.view :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Title"
    :key   :title
    :type  :text}
   {:label :avatar
    :key   :avatar
    :type  :boolean}
   {:label   "Description type:"
    :key     :description
    :type    :select
    :options [{:key   :none
               :value nil}
              {:key   :description
               :value :description}]}
   {:label "Description text"
    :key   :description-props
    :type  :text}])

(defn state->text-combinations-props
  [state]
  (if (:avatar state)
    (assoc state :avatar (resources/get-mock-image :user-picture-male4))
    state))

(defn cool-preview
  []
  (let [state (reagent/atom {:title                           "Title"
                             :title-accessibility-label       :title
                             :description                     nil
                             :description-props               ""
                             :description-accessibility-label :subtitle})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         {:padding-vertical 60
          :align-items      :center}
         [quo2/view
          (state->text-combinations-props @state)]]]])))

(defn preview
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :flex-grow                    1
     :nested-scroll-enabled        true
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
