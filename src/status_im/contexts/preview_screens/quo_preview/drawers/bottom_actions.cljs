(ns status-im.contexts.preview-screens.quo-preview.drawers.bottom-actions
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

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
   {:label   "Button 1 type"
    :type    :select
    :key     :type
    :options button-options
    :path    [:button-one-props]}
   {:label "Button 1 disabled?"
    :type  :boolean
    :key   :disabled?
    :path  [:button-one-props]}
   {:label   "Button 2 type"
    :type    :select
    :key     :type
    :options button-options
    :path    [:button-two-props]}
   {:label "Button 2 disabled?"
    :type  :boolean
    :key   :disabled?
    :path  [:button-two-props]}
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
                             :button-one-props {:on-press  (button-press 1)
                                                :type      :primary
                                                :icon-left :i/arrow-up}
                             :button-two-props {:on-press (button-press 2)
                                                :type     :grey}
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
