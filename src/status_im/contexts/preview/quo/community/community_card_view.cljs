(ns status-im.contexts.preview.quo.community.community-card-view
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview.quo.preview :as preview]
    [utils.i18n :as i18n]))

(def community-data
  {:id "id"
   :name "Status"
   :description
   "Status is a secure messaging app, crypto wallet and web3 browser built with the state of the art technology"
   :cover (resources/get-mock-image :community-cover)
   :community-icon (resources/get-mock-image :status-logo)
   :customization-color :blue
   :tokens [{:id 1 :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}]}]
   :tags [{:id    1
           :name  (i18n/label :t/music)
           :emoji (resources/get-image :music)}
          {:id    2
           :name  (i18n/label :t/lifestyle)
           :emoji "🧩"}
          {:id    3
           :name  (i18n/label :t/podcasts)
           :emoji "🎶"}]})

(def descriptor
  [{:key     :status
    :type    :select
    :options [{:key :gated}
              {:key :open}]}
   {:key  :locked?
    :type :boolean}
   {:key  :loading?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:status   :gated
                             :locked?  true
                             :loading? false})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/community-card-view-item
        {:community (merge @state community-data)
         :loading?  (:loading? @state)}]])))
