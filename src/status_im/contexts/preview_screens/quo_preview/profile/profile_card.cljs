(ns status-im.contexts.preview-screens.quo-preview.profile.profile-card
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key :keycard-account? :type :boolean}
   {:key :show-emoji-hash? :type :boolean}
   {:key :show-user-hash? :type :boolean}
   {:key :show-options-button? :type :boolean}
   {:key :show-logged-in? :type :boolean}
   {:key :login-card? :type :boolean}
   {:key :last-item? :type :boolean}
   {:key :name :type :text}
   {:key :hash :type :text}
   {:key :emoji-hash :type :text}
   (preview/customization-color-option)])

(defn view
  []
  (let [state (reagent/atom {:keycard-account? false
                             :name "Matt Grote"
                             :on-options-press nil
                             :on-card-press nil
                             :show-options-button? true
                             :show-logged-in? true
                             :show-user-hash? false
                             :login-card? false
                             :last-item? true
                             :on-press-sign nil
                             :customization-color :turquoise
                             :profile-picture (resources/get-mock-image :user-picture-male5)
                             :show-emoji-hash? false
                             :hash "zQ3k83euenmcikw7474hfu73t5N"
                             :emoji-hash "😄😂🫣🍑😇🤢😻🥷🏻🦸🏻‍♀️🦸🏻🦸🏻‍♂️🦹🏻‍♀️🧑🏻‍🎄🎅🏻"})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-bottom 50}}
       [quo/profile-card @state]])))
