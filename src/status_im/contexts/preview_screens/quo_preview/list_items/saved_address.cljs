(ns status-im.contexts.preview-screens.quo-preview.list-items.saved-address
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [(preview/customization-color-option {:key :account-color})
   {:key :blur? :type :boolean}
   {:key :title :type :text}
   (preview/customization-color-option)])

(defn view
  []
  (let [state (reagent/atom {:type                :default
                             :customization-color :blue
                             :account-color       :flamingo
                             :title               "Alisher Yakupov"
                             :address             "0x21a...49e"
                             :on-options-press    #(js/alert "Options button pressed!")})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true
        :blur-dark-only?       true}
       [quo/saved-address
        (assoc @state
               :user-props
               {:name                (:title @state)
                :address             (:address @state)
                :emoji               (:emoji @state)
                :customization-color (:account-color @state)})]])))
