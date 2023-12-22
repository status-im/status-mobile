(ns status-im.contexts.preview-screens.quo-preview.tags.network-tags
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as resources]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def community-networks
  [[{:source (resources/get-network :ethereum)}]
   [{:source (resources/get-network :arbitrum)}
    {:source (resources/get-network :ethereum)}]
   [{:source (resources/get-network :arbitrum)}
    {:source (resources/get-network :optimism)}
    {:source (resources/get-network :ethereum)}]])

(def descriptor
  [{:type    :select
    :key     :status
    :options [{:key :error}
              {:key :default}]}
   {:type :text
    :key  :title}
   {:type    :select
    :key     :networks
    :options [{:key 1}
              {:key 2}
              {:key 3}]}
   {:type :boolean
    :key  :blur?}])


(defn view
  []
  (let [state (reagent/atom {:title    "Tag"
                             :status   :default
                             :networks 3})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true}
       [rn/view
        {:style {:align-self      :center
                 :justify-content :center
                 :flex            1}}
        [quo/network-tags
         (assoc @state
                :networks
                (nth community-networks (dec (:networks @state))))]]])))
