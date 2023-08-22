(ns status-im2.contexts.quo-preview.empty-state.empty-state
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key  :title
    :type :text}
   {:key  :description
    :type :text}
   {:key     :image
    :type    :select
    :options [{:key :no-contacts-light}
              {:key :no-contacts-dark}
              {:key :no-messages-light}
              {:key :no-messages-dark}]}
   {:key  :upper-button-text
    :type :text}
   {:key  :lower-button-text
    :type :text}
   {:key  :blur?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:image             :no-messages-light
                             :title             "A big friendly title"
                             :description       "Some cool description will be here"
                             :blur?             false
                             :upper-button-text "Send community link"
                             :lower-button-text "Invite friends to Status"})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :blur-height           300
        :show-blur-background? true}
       [quo/empty-state
        (-> @state
            (assoc :upper-button
                   {:text     (:upper-button-text @state)
                    :on-press #(js/alert "Upper button")})
            (assoc :lower-button
                   {:text     (:lower-button-text @state)
                    :on-press #(js/alert "Lower button")})
            (update :image resources/get-image))]])))
