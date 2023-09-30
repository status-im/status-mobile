(ns status-im2.contexts.quo-preview.drawers.bottom-actions
  (:require [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [quo2.core :as quo]))

(def button-two "Cancel")
(def button-one "Request to join")
(def description "Joining the community will reveal your public addresses to the node owner")
(def button-options
  [{:key :primary}
   {:key :grey}
   {:key :danger}
   {:key :positive}])
(defn button-press
  [id]
  #(js/alert (str "Button " id " Pressed")))

(def descriptor
  [{:type    :select
    :key     :actions
    :options [{:key :1-action}
              {:key :2-actions}]}
   {:type    :select
    :key     :button-two-type
    :options button-options}
   {:type    :select
    :key     :button-one-type
    :options button-options}
   {:key  :description
    :type :text}
   {:key  :button-one-label
    :type :text}
   {:key  :button-two-label
    :type :text}
   {:key  :scroll?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:actions          :2-actions
                             :description      description
                             :button-one-label button-one
                             :button-two-label button-two
                             :button-one-press (button-press 2)
                             :button-two-press (button-press 1)
                             :button-one-type  :primary
                             :button-two-type  :grey
                             :scroll?          false})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :blur?                     (:scroll? @state)
        :show-blur-background?     true
        :blur-dark-only?           true
        :component-container-style {:margin-top         40
                                    :padding-horizontal 0}}
       [quo/bottom-actions @state]])))
