(ns status-im2.contexts.quo-preview.onboarding.small-option-card
  (:require
    [quo.react-native :as rn]
    [quo2.components.onboarding.small-option-card.view :as quo2]
    [quo2.foundations.colors :as colors]
    [reagent.core :as reagent]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Variant"
    :key     :variant
    :type    :select
    :options [{:key   :main
               :value "Main"}
              {:key   :icon
               :value "Icon"}]}
   {:label   "Image"
    :key     :image
    :type    :select
    :options [{:key   (resources/get-mock-image :small-opt-card-main)
               :value "Image 1"}
              {:key   (resources/get-mock-image :small-opt-card-icon)
               :value "Image 2"}]}
   {:label "Title"
    :key   :title
    :type  :text}
   {:label "Subtitle"
    :key   :subtitle
    :type  :text}])

(defn preview-small-option-card
  []
  (let [state (reagent/atom {:variant  :main
                             :image    (-> descriptor second :options first :key)
                             :title    "Generate keys "
                             :subtitle "Your new self-sovereign identity in Status"
                             :on-press #(js/alert "Small option card pressed!")})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view {:style {:padding-bottom 150}}
        [rn/view
         {:style {:background-color colors/neutral-80
                  :padding          20}}
         [quo2/small-option-card @state]]]])))
