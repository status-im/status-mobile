(ns status-im.contexts.preview-screens.quo-preview.onboarding.small-option-card
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :variant
    :type    :select
    :options [{:key :main}
              {:key :icon}]}
   {:key     :image
    :type    :select
    :options [{:key   (resources/get-mock-image :small-opt-card-main)
               :value "Image 1"}
              {:key   (resources/get-mock-image :small-opt-card-icon)
               :value "Image 2"}]}
   {:key :title :type :text}
   {:key :subtitle :type :text}
   {:key :button-label :type :text}])

(defn view
  []
  (let [state (reagent/atom {:variant      :main
                             :image        (-> descriptor second :options first :key)
                             :title        "Generate keys "
                             :subtitle     "Your new self-sovereign identity in Status"
                             :button-label "Let's go!"
                             :on-press     #(js/alert "Small option card pressed!")})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding          20
                                    :background-color colors/neutral-80}}
       [quo/small-option-card @state]])))
