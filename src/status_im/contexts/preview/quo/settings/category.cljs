(ns status-im.contexts.preview.quo.settings.category
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview.quo.preview :as preview]))

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

(defn create-data-item-array
  [n]
  (vec
   (for [i (range n)]
     {:blur?         false
      :description   :default
      :icon-right?   true
      :right-icon    :i/chevron-right
      :icon-color    colors/neutral-10
      :card?         false
      :label         :preview
      :status        :default
      :size          :default
      :right-content {:type :accounts
                      :data [{:emoji "🔥" :customization-color :yellow}]}
      :title         (str "Item title " i)
      :subtitle      "Item subtitle"})))

(def descriptor
  [{:key :blur? :type :boolean}
   {:key     :list-type
    :type    :select
    :options [{:key   :settings
               :value :settings}
              {:key   :reorder
               :value :reorder}
              {:key   :data-item
               :value :data-item}]}])

(def ^:constant n-items 5)

(defn view
  []
  (let [state (reagent/atom {:label     "Label"
                             :blur?     false
                             :list-type :settings})]
    (fn []
      (let [list-type (:list-type @state)
            data      (if (= list-type :data-item)
                        (create-data-item-array n-items)
                        (create-item-array n-items))]
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
