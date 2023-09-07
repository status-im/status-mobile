(ns status-im2.contexts.quo-preview.profile.profile-card
  (:require [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [quo2.core :as quo]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Show: Is from key card?"
    :key   :keycard-account?
    :type  :boolean}
   {:label "Show: Emoji hash?"
    :key   :show-emoji-hash?
    :type  :boolean}
   {:label "Show: User hash?"
    :key   :show-user-hash?
    :type  :boolean}
   {:label "Show: Options Button?"
    :key   :show-options-button?
    :type  :boolean}
   {:label "Show: Logged In?"
    :key   :show-logged-in?
    :type  :boolean}
   {:label "Login Card?"
    :key   :login-card?
    :type  :boolean}
   {:label "Last Item?"
    :key   :last-item?
    :type  :boolean}
   {:label   "Customization Color"
    :key     :customization-color
    :type    :select
    :options (map (fn [[color-kw _]]
                    {:key   color-kw
                     :value (name color-kw)})
                  colors/customization)}
   {:label "Name"
    :key   :name
    :type  :text}
   {:label "Hash"
    :key   :hash
    :type  :text}
   {:label "Emoji hash"
    :key   :emoji-hash
    :type  :text}])

(defn preview-profile-card
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
       {:state      state
        :descriptor descriptor}
       [rn/view {:padding-bottom 150}
        [rn/view
         {:padding-vertical 60
          :flex-direction   :row
          :justify-content  :center}
         [quo/profile-card @state]]]])))
