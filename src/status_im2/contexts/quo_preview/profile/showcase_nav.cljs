(ns status-im2.contexts.quo-preview.profile.showcase-nav
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def generate-nav-data
  (->> (range 15)
       (map inc)
       (mapv (fn [index]
               {:icon (rand-nth [:i/recent :i/profile :i/communities :i/wallet :i/nft :i/token
                                 :i/delete])
                :id   index}))))

(def descriptor
  [{:type :number
    :key  :previews-length}
   {:type    :select
    :key     :state
    :options [{:key   :default
               :value "default"}
              {:key   :scroll
               :value "scroll"}]}])

(defn view
  []
  (let [state           (reagent/atom {:state           :default
                                       :previews-length 10
                                       :active-id       0})
        component-state (reagent/cursor state [:state])
        previews-length (reagent/cursor state [:previews-length])
        active-id       (reagent/cursor state [:active-id])
        nav-data        generate-nav-data]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-horizontal 0}}
       [quo/showcase-nav
        {:default-active 3
         :state          @component-state
         :active-id      @active-id
         :data           (take (or @previews-length 1) nav-data)
         :on-press       #(swap! state assoc :active-id %)}]])))
