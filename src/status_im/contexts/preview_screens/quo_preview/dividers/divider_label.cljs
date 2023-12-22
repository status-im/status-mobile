(ns status-im.contexts.preview-screens.quo-preview.dividers.divider-label
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key  :label
    :type :text}
   {:key     :chevron
    :type    :select
    :options [{:key   :left
               :value "Left"}
              {:key   :right
               :value "Right"}
              {:key   nil
               :value "None"}]}
   {:key     :chevron-icon
    :type    :select
    :options [{:key   :i/chevron-down
               :value "Chevron Down"}
              {:key   :i/chevron-right
               :value "Chevron Right"}]}
   {:key  :tight?
    :type :boolean}
   {:key  :counter?
    :type :boolean}
   {:key  :counter-value
    :type :text}
   {:key  :blur?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:label         "Welcome"
                             :chevron       nil
                             :chevron-icon  nil
                             :tight?        true
                             :counter?      false
                             :counter-value 0
                             :blur?         false})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true}
       [quo/divider-label
        (assoc @state
               :on-press
               #(js/alert "Divider label pressed!"))
        (:label @state)]])))
