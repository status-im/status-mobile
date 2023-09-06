(ns status-im2.contexts.quo-preview.settings.settings-list
  (:require [quo2.components.settings.settings-list.view :as quo]
            [react-native.core :as rn]
            [status-im2.common.resources :as resources]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Account name:"
    :key   :title
    :type  :text}
   {:label "Chevron:"
    :key   :chevron?
    :type  :boolean}
   {:label "Badge:"
    :key   :badge?
    :type  :boolean}
   {:label "Toggle:"
    :key   :toggle-props
    :type  :boolean}
   {:label "Communities"
    :key   :communities-props
    :type  :boolean}
   {:label "Button"
    :key   :button-props
    :type  :boolean}
   {:label "Status Tag"
    :key   :status-tag-props
    :type  :boolean}])

(defn get-props
  [data]
  (when (:toggle-props data) (js/console.warn data))
  (merge
   data
   {:toggle-props (when (:toggle-props data)
                    {:checked?  true
                     :on-change (fn [new-value] (js/alert new-value))})
    :button-props (when (:button-props data)
                    {:title "Button" :on-press (fn [] (js/alert "Button pressed"))})
    :communities-props
    (when (:communities-props data)
      {:data
       [{:source (resources/mock-images :rarible)}
        {:source (resources/mock-images :decentraland)}
        {:source (resources/mock-images :coinbase)}]})
    :status-tag-props (when (:status-tag-props data)
                        {:size           :small
                         :status         {:type :positive}
                         :no-icon?       true
                         :label          "example"
                         :override-theme :dark})}))

(defn preview-settings-list
  []
  (let [state (reagent/atom {:title               "Account"
                             :accessibility-label :settings-list-item
                             :left-icon           :browser-context
                             :chevron?            true
                             :on-press            (fn [] (js/alert "Settings list item pressed"))})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view
        {:padding-vertical   100
         :padding-horizontal 20
         :align-items        :center}
        [quo/settings-list (get-props @state)]]])))
