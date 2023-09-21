(ns status-im2.contexts.quo-preview.buttons.button
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :primary}
              {:key :positive}
              {:key :grey}
              {:key :dark-grey}
              {:key :outline}
              {:key :ghost}
              {:key :danger}
              {:key :black}]}
   {:key     :size
    :type    :select
    :options [{:key   56
               :value "56"}
              {:key   40
               :value "40"}
              {:key   32
               :value "32"}
              {:key   24
               :value "24"}]}
   {:key     :background
    :type    :select
    :options [{:key :blur}
              {:key :photo}]}
   {:key  :icon-only?
    :type :boolean}
   {:key  :icon-top
    :type :boolean}
   {:key  :icon-right
    :type :boolean}
   {:key  :icon-left
    :type :boolean}
   {:key  :disabled?
    :type :boolean}
   {:key  :label
    :type :text}
   (preview/customization-color-option)])

(defn view
  []
  (let [state      (reagent/atom {:label               "Press Me"
                                  :size                40
                                  :type                :primary
                                  :customization-color :blue})
        label      (reagent/cursor state [:label])
        icon-left  (reagent/cursor state [:icon-left])
        icon-right (reagent/cursor state [:icon-right])
        icon-top   (reagent/cursor state [:icon-top])
        icon-only? (reagent/cursor state [:icon-only?])]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:align-items :center}}
       (when (= :photo (:background @state))
         [rn/image
          {:source (resources/get-mock-image :community-cover)
           :style  {:position :absolute
                    :top      0
                    :left     0
                    :right    0
                    :bottom   0}}])
       [quo/button
        (merge (dissoc @state
                :theme
                :customization-color
                :icon-left
                :icon-right)
               {:background (:background @state)
                :on-press   #(println "Hello world!")}
               (when (= :primary (:type @state)) {:customization-color (:customization-color @state)})
               (when @icon-top
                 {:icon-top :i/placeholder})
               (when @icon-left
                 {:icon-left :i/placeholder})
               (when @icon-right
                 {:icon-right :i/placeholder}))
        (if @icon-only? :i/placeholder @label)]])))
