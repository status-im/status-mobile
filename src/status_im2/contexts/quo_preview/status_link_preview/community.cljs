(ns status-im2.contexts.quo-preview.status-link-preview.community
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:type :text :key :title}
   {:type :text :key :description}
   {:type :boolean :key :loading?}
   {:type :text :key :members-count}
   {:type :text :key :active-members-count}])

(defn view
  []
  (let [state (reagent/atom
               {:title                "Doodles"
                :description          "Coloring the world with joy • ᴗ •"
                :members-count        24
                :active-members-count 12
                :link                 "https://status.app/community-link"
                :loading?             false})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [quo/status-link-preview-community
        {:title                (:title @state)
         :description          (:description @state)
         :icon                 (resources/get-mock-image :status-logo)
         :banner               (resources/get-mock-image :status-logo)
         :loading?             (:loading? @state)
         :members-count        (int (:members-count @state))
         :loading-message      (:loading-message @state)
         :active-members-count (int (:active-members-count @state))
         :on-clear             #(js/alert "Clear button pressed")}]])))
