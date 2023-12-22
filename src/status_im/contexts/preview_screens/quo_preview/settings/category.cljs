(ns status-im.contexts.preview-screens.quo-preview.settings.category
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(defn create-item-array
  [n]
  (vec
   (for [i (range n)]
     {:title       (str "Item " i)
      :subtitle    "subtitle"
      :action      :arrow
      :right-icon  :i/globe
      :image       (resources/get-mock-image :diamond)
      :image-props :i/browser
      :image-size  32})))

(def descriptor
  [{:key :blur? :type :boolean}
   {:key     :list-type
    :type    :select
    :options [{:key   :settings
               :value :settings}
              {:key   :reorder
               :value :reorder}]}])

(defn view
  []
  (let [state (reagent/atom {:label     "Label"
                             :blur?     false
                             :list-type :settings})]
    (fn []
      (let [data (create-item-array 5)]
        [preview/preview-container
         {:state                 state
          :descriptor            descriptor
          :blur?                 (:blur? @state)
          :show-blur-background? true
          :blur-dark-only?       true
          :blur-height           400}
         [quo/category
          {:list-type (:list-type @state)
           :label     (:label @state)
           :data      data
           :blur?     (:blur? @state)}]]))))
