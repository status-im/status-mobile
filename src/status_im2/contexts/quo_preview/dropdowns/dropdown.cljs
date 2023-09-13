(ns status-im2.contexts.quo-preview.dropdowns.dropdown
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :primary}
              {:key :secondary}
              {:key :grey}
              {:key :dark-grey}
              {:key :outline}
              {:key :ghost}
              {:key :danger}
              {:key :positive}]}
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
   {:key  :icon
    :type :boolean}
   {:label "Before icon:"
    :key   :before
    :type  :boolean}
   {:key  :disabled
    :type :boolean}
   {:key  :label
    :type :text}])

(defn view
  []
  (let [state  (reagent/atom {:label "Press Me"
                              :size  40})
        label  (reagent/cursor state [:label])
        before (reagent/cursor state [:before])
        icon   (reagent/cursor state [:icon])]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:align-items :center}}
       [quo/dropdown
        (merge (dissoc @state
                :theme
                :before
                :after)
               {:on-press #(println "Hello world!")}
               (when @before
                 {:before :i/placeholder}))
        (if @icon :i/placeholder @label)]])))
