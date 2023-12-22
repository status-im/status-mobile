(ns status-im.contexts.preview-screens.quo-preview.list-items.saved-contact-address
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key :accounts-number :type :number}
   {:key :account-emoji :type :text}
   {:key :account-name :type :text}
   {:key :full-name :type :text}
   {:key :show-alert-on-press? :type :boolean}
   {:key :active-state? :type :boolean}
   (preview/customization-color-option {:key :account-color})
   (preview/customization-color-option)])

(defn view
  []
  (let [state (reagent/atom {:customization-color  :blue
                             :accounts-number      1
                             :account-name         "New House"
                             :account-address      "0x21a...49e"
                             :account-emoji        "🍔"
                             :account-color        :blue
                             :active-state?        true
                             :full-name            "Mark Villacampa"
                             :show-alert-on-press? false})]
    (fn []
      (let [account-props {:name                (:account-name @state)
                           :address             (:account-address @state)
                           :emoji               (:account-emoji @state)
                           :customization-color (:account-color @state)}
            contact-props {:full-name           (:full-name @state)
                           :profile-picture     (resources/get-mock-image
                                                 :user-picture-male4)
                           :customization-color :purple}]
        [preview/preview-container
         {:state      state
          :descriptor descriptor}
         [quo/saved-contact-address
          (merge @state
                 {:accounts      (repeat (:accounts-number @state) account-props)
                  :contact-props contact-props}
                 (when (:show-alert-on-press? @state)
                   {:on-press #(js/alert "Pressed!")}))]]))))
