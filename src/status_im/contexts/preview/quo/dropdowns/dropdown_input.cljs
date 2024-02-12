(ns status-im.contexts.preview.quo.dropdowns.dropdown-input
  (:require
    [quo.core :as quo]
    [utils.reagent :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key     :state
    :type    :select
    :options [{:key :default}
              {:key :active}
              {:key :disabled}]}
   {:key  :icon?
    :type :boolean}
   {:key  :label?
    :type :boolean}
   {:key  :blur?
    :type :boolean}
   {:key  :header-label
    :type :text}
   {:key  :label
    :type :text}])

(defn view
  []
  (let [state (reagent/atom {:state        :default
                             :label        "Dropdown"
                             :header-label "Label"
                             :icon?        true
                             :label?       true
                             :blur?        false})
        label (reagent/cursor state [:label])
        blur? (reagent/cursor state [:blur?])]
    [:f>
     (fn []
       [preview/preview-container
        {:state                     state
         :descriptor                descriptor
         :component-container-style (when-not @blur?
                                      {:align-items       :center
                                       :margin-horizontal 30})
         :blur-container-style      {:align-items :center}
         :blur?                     @blur?
         :show-blur-background?     true}
        [quo/dropdown-input
         (assoc @state :on-press #(js/alert "Pressed dropdown"))
         @label]])]))
