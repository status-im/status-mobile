(ns status-im2.contexts.quo-preview.tags.network-tags
  (:require [quo2.components.tags.network-tags.view :as quo2]
            [quo2.foundations.colors :as colors]
            [quo2.foundations.resources :as resources]
            [react-native.core :as rn]
            [reagent.core :as reagent]))

(def community-networks
  [{:title "Tags" :status :error :networks [{:source (resources/get-network :ethereum)}]}
   {:title    "Tags"
    :status   :default
    :networks [{:source (resources/get-network :ethereum)}
               {:source (resources/get-network :arbitrum)}]}
   {:title    "Tags"
    :status   :default
    :networks [{:source (resources/get-network :ethereum)}
               {:source (resources/get-network :arbitrum)}
               {:source (resources/get-network :optimism)}]}])

(defn cool-preview
  []
  (let [state (reagent/atom {:size 32})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view
         {:padding-vertical 60
          :align-self       :center
          :justify-content  :center}
         (when @state
           (for [{:keys [networks title status]} community-networks]
             ^{:key networks}
             [rn/view
              {:margin-top 20
               :align-self :flex-end}
              [quo2/network-tags
               (merge @state
                      {:networks networks
                       :status   status
                       :title    title})]]))]]])))

(defn preview-network-tags
  []
  [rn/view
   {:background-color (colors/theme-colors
                       colors/white
                       colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
