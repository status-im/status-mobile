(ns status-im2.contexts.quo-preview.text-combinations.preview
  (:require [quo2.components.text-combinations.view :as quo2]
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

(defn preview
  []
  (let [state (reagent/atom {:title                           "Title"
                             :title-accessibility-label       :title
                             :description                     nil
                             :description-props               ""
                             :description-accessibility-label :subtitle})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view {:padding-bottom 150}
        [rn/view
         {:padding-vertical 60
          :align-items      :center}
         [quo2/view
          (state->text-combinations-props @state)]]]])))
