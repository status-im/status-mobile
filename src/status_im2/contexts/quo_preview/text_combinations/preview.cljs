(ns status-im2.contexts.quo-preview.text-combinations.preview
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key :title :type :text}
   {:key :avatar :type :boolean}
   {:key  :description
    :type :text}])

(defn state->text-combinations-props
  [state]
  (if (:avatar state)
    (assoc state :avatar (resources/get-mock-image :user-picture-male4))
    state))

(defn view
  []
  (let [state (reagent/atom {:title                           "Title"
                             :title-accessibility-label       :title
                             :description                     ""
                             :description-accessibility-label :subtitle})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-vertical 60}}
       [quo/text-combinations (state->text-combinations-props @state)]])))
