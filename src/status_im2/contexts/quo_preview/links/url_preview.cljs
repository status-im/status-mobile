(ns status-im2.contexts.quo-preview.links.url-preview
  (:require
    [quo2.core :as quo]
    [reagent.core :as reagent]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:type :text :key :title}
   {:type :text :key :body}
   {:type :boolean :key :with-logo?}
   {:type :boolean :key :loading?}
   {:type :text :key :loading-message}])

(defn view
  []
  (let [state (reagent/atom
               {:title           "Status - Private, Secure Communication"
                :body            "Status.im"
                :with-logo?      true
                :loading?        false
                :loading-message "Generating preview"})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/url-preview
        {:title           (:title @state)
         :body            (:body @state)
         :logo            (when (:with-logo? @state)
                            (resources/get-mock-image :status-logo))
         :loading?        (:loading? @state)
         :loading-message (:loading-message @state)
         :on-clear        #(js/alert "Clear button pressed")}]])))
