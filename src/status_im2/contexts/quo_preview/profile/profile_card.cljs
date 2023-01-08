(ns status-im2.contexts.quo-preview.profile.profile-card
  (:require [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [quo2.core :as quo]
            [status-im.react-native.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Show: Sign in to this profile?"
    :key   :show-sign-profile?
    :type  :boolean}
   {:label "Show: Is from key card?"
    :key   :key-card?
    :type  :boolean}
   {:label "Show: Emoji hash?"
    :key   :show-emoji-hash?
    :type  :boolean}
   {:label   "Customization Color"
    :key     :customization-color
    :type    :select
    :options [{:key   :primary
               :value "Primary"}
              {:key   :purple
               :value "Purple"}
              {:key   :indigo
               :value "Indigo"}
              {:key   :turquoise
               :value "Turquoise"}
              {:key   :blue
               :value "Blue"}
              {:key   :green
               :value "Green"}
              {:key   :yellow
               :value "Yellow"}
              {:key   :orange
               :value "Orange"}
              {:key   :red
               :value "Red"}
              {:key   :pink
               :value "Pink"}
              {:key   :brown
               :value "Brown"}
              {:key   :beige
               :value "Beige"}]}
   {:label "Name"
    :key   :name
    :type  :text}
   {:label "Hash"
    :key   :hash
    :type  :text}
   {:label "Emoji hash"
    :key   :emoji-hash
    :type  :text}
   {:label "Sign button label"
    :key   :sign-label
    :type  :text}])

(defn cool-preview
  []
  (let [state (reagent/atom {:show-sign-profile? true
                             :key-card? true
                             :name "Matt Grote"
                             :sign-label "Sign in to this profile"
                             :on-press-dots nil
                             :on-press-sign nil
                             :customization-color :turquoise
                             :profile-picture (resources/get-mock-image :user-picture-male5)
                             :show-emoji-hash? true
                             :hash "zQ3k83euenmcikw7474hfu73t5N"
                             :emoji-hash "😄😂🫣🍑😇🤢😻🥷🏻🦸🏻‍♀️🦸🏻🦸🏻‍♂️🦹🏻‍♀️🧑🏻‍🎄🎅🏻"})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view {:flex 1}
         [preview/customizer state descriptor]]
        [rn/view
         {:padding-vertical  60
          :flex-direction    :row
          :margin-horizontal 20
          :justify-content   :center}
         [quo/profile-card @state]]]])))

(defn preview-profile-card
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white
                                           colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                      1
     :keyboardShouldPersistTaps :always
     :header                    [cool-preview]
     :key-fn                    str}]])
