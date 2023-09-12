(ns status-im2.contexts.quo-preview.settings.category
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [quo2.theme :as theme]
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]
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
  (let [state                  (reagent/atom {:label       "Label"
                                              :size        "5"
                                              :blur?       false
                                              :right-icon? true
                                              :image?      true
                                              :subtitle?   true
                                              :list-type   :settings})
        {:keys [width height]} (rn/get-window)]
    (fn []
      (let [data (reagent/atom (create-item-array (max (js/parseInt (:size @state)) 1) @state))]
        [:f>
         (fn []
           (rn/use-effect (fn []
                            (if (:blur? @state)
                              (theme/set-theme :dark)
                              (theme/set-theme :light))
                            (reset! data (create-item-array (max (js/parseInt (:size @state)) 1)
                                                            @state)))
                          [(:blur? @state) (:right-icon? @state) (:image? @state) (:subtitle? @state)])
           [preview/preview-container
            {:state      state
             :descriptor (if (= (:list-type @state) :settings) settings-descriptor reorder-descriptor)}
            [rn/view
             {:style {:flex             1
                      :padding-bottom   150
                      :margin-bottom    50
                      :background-color (colors/theme-colors colors/neutral-5 colors/neutral-95)}}
             [rn/view
              {:style {:min-height 200
                       :z-index    1}}
              [preview/customizer state
               (if (= (:list-type @state) :settings) settings-descriptor reorder-descriptor)]]
             (when (:blur? @state)
               [fast-image/fast-image
                {:source (resources/get-mock-image :dark-blur-bg)
                 :style  {:width    width
                          :height   height
                          :position :absolute}}])
             [rn/view
              {:style {:background-color (if (:blur? @state)
                                           colors/neutral-80-opa-80
                                           (colors/theme-colors colors/neutral-5 colors/neutral-95))}}
              [quo/category
               {:list-type (:list-type @state)
                :label     (:label @state)
                :data      @data
                :blur?     (:blur? @state)}]]]])]))))
