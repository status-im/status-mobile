(ns status-im.contexts.preview.quo.tags.collectible-tag
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key     :size
    :type    :select
    :options [{:key   :size-24
               :value "Size 24"}
              {:key   :size-32
               :value "Size 32"}]}
   {:key     :options
    :type    :select
    :options [{:key   :add
               :value :add}
              {:key   :hold
               :value :hold}]}
   {:key  :blur?
    :type :boolean}
   {:key  :collectible-name
    :type :text}
   {:key  :collectible-id
    :type :text}])

(defn view
  []
  (let [state (reagent/atom {:size                :size-24
                             :collectible-name    "Collectible"
                             :collectible-id      "#123"
                             :collectible-img-src (resources/mock-images :collectible)
                             :blur?               false})]
    (fn []
      [preview/preview-container
       {:state                 state
        :blur?                 (:blur? @state)
        :show-blur-background? true
        :descriptor            descriptor}
       [rn/view {:style {:align-items :center}}
        [quo/collectible-tag @state]]])))
