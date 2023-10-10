(ns status-im2.contexts.quo-preview.settings.category
  (:require
    [quo2.core :as quo]
    [reagent.core :as reagent]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.quo-preview.preview :as preview]))

(defn create-item-array
  [n {:keys [right-icon? image? subtitle? list-type]}]
  (vec
   (for [i (range n)]
     {:title       (str "Item " i)
      :subtitle    (when subtitle? "subtitle")
      :action      :arrow
      :right-icon  (when right-icon? :i/globe)
      :image       (if (= list-type :settings) :icon (when image? (resources/get-mock-image :diamond)))
      :image-props :i/browser
      :image-size  (if image? 32 0)})))

(def reorder-descriptor
  [{:label "Right icon:"
    :key   :right-icon?
    :type  :boolean}
   {:label "Image:"
    :key   :image?
    :type  :boolean}
   {:label "Subtitle:"
    :key   :subtitle?
    :type  :boolean}
   {:label "Blur:"
    :key   :blur?
    :type  :boolean}
   {:label   "List type:"
    :key     :list-type
    :type    :select
    :options [{:key :settings :value :settings} {:key :reorder :value :reorder}]}])

(def settings-descriptor
  [{:label "Blur:"
    :key   :blur?
    :type  :boolean}
   {:label   "List type:"
    :key     :list-type
    :type    :select
    :options [{:key :settings :value :settings} {:key :reorder :value :reorder}]}])

(defn preview
  []
  (let [state (reagent/atom {:label       "Label"
                             :size        "5"
                             :blur?       false
                             :right-icon? true
                             :image?      true
                             :subtitle?   true
                             :list-type   :settings})]
    (fn []
      (let [data (reagent/atom (create-item-array (max (js/parseInt (:size @state)) 1) @state))]
        [preview/preview-container
         {:state                 state
          :descriptor            (if (= (:list-type @state) :settings)
                                   settings-descriptor
                                   reorder-descriptor)
          :blur?                 (:blur? @state)
          :show-blur-background? true
          :blur-dark-only?       true
          :blur-height           400}
         [quo/category
          {:list-type (:list-type @state)
           :label     (:label @state)
           :data      @data
           :blur?     (:blur? @state)}]]))))
