(ns status-im.contexts.preview.quo.avatars.token-avatar
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as foundations.resources]
    [react-native.core :as rn]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:type    :select
    :key     :type
    :options [{:key :asset}
              {:key :collectible}]}
   {:type :boolean
    :key  :context?}])

(defn view
  []
  (let [[state set-state] (rn/use-state {:type     :asset
                                         :context? false})]
    [preview/preview-container
     {:state      state
      :set-state  set-state
      :descriptor descriptor}
     [quo/token-avatar
      (assoc state
             :network-image (foundations.resources/get-network :optimism)
             :image         (resources/get-mock-image
                             (if (= (state :type) :asset) :status-logo :collectible1)))]]))
