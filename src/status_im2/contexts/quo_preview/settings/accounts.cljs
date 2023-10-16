(ns status-im2.contexts.quo-preview.settings.accounts
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [(preview/customization-color-option)
   {:key :account-name :type :text}
   {:key :account-address :type :text}])

(defn view
  []
  (let [state (reagent/atom {:customization-color :blue
                             :account-name        "Booze for Dubai"
                             :account-address     "0x21a ... 49e"
                             :avatar-icon         :i/placeholder
                             :on-press-menu       (fn []
                                                    (js/alert "Menu button pressed"))})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-vertical 100
                                    :align-items      :center}}
       [quo/account @state]])))
