(ns status-im.contexts.preview.quo.profile.collectible-list-item
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview.quo.preview :as preview]))

(defonce test-image (resources/get-mock-image :collectible))
(defonce test-avatar (resources/get-mock-image :monkey))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :card}
              {:key :image}]}
   {:key  :community?
    :type :boolean}
   {:key     :gradient-color-index
    :type    :select
    :options [{:key :gradient-1}
              {:key :gradient-2}
              {:key :gradient-3}
              {:key :gradient-4}
              {:key :gradient-5}]}
   {:key  :counter
    :type :text}
   {:key  :collectible-name
    :type :text}])

(defn view
  []
  (let [state (reagent/atom {:type                 :card
                             :collectible-name     "Doodle #6822"
                             :gradient-color-index :gradient-1
                             :community?           false
                             :counter              ""})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-vertical  20
                                    :margin-horizontal 95}}
       [quo/collectible-list-item
        (assoc @state
               :counter              (when (seq (:counter @state)) (:counter @state))
               :gradient-color-index (:gradient-color-index @state)
               :image-src            test-image
               :supported-file?      true
               :avatar-image-src     test-avatar
               :on-press             #(js/alert "Pressed"))]])))
