(ns status-im.contexts.preview.quo.empty-state.empty-state
  (:require
    [quo.context]
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key  :title
    :type :text}
   {:key  :description
    :type :text}
   {:key     :image
    :type    :select
    :options [{:key :cat-in-box}
              {:key :no-contacts}]}
   {:key  :upper-button-text
    :type :text}
   {:key  :lower-button-text
    :type :text}
   {:key  :blur?
    :type :boolean}])

(defn view
  []
  (let [theme (quo.context/use-theme)
        state (reagent/atom {:image             :no-contacts
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
            (assoc :image (resources/get-themed-image (:image @state) theme)))]])))
