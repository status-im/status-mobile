(ns quo2.screens.avatars.channel-avatar
  (:require [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [quo2.foundations.colors :as colors]
            [quo2.components.avatars.channel-avatar :as quo2]))

(def descriptor [{:label "Big?"
                  :key   :big?
                  :type  :boolean}
                 {:label "Avatar color"
                  :key   :emoji-background-color
                  :type  :text}
                 {:label "Avatar color"
                  :key   :emoji
                  :type  :text}
                 {:label   "is Locked?"
                  :key     :locked?
                  :type    :select
                  :options [{:key   nil
                             :value "None"}
                            {:key  false
                             :value "Unlocked"}
                            {:key   true
                             :value "Locked"}]}])

(defn cool-preview []
  (let [state (reagent/atom {:big?                   true
                             :locked?                nil
                             :emoji                  "🍑"
                             :emoji-background-color :gray})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view {:flex 1}
         [preview/customizer state descriptor]]
        [rn/view {:padding-vertical 60
                  :flex-direction   :row
                  :justify-content  :center}
         [quo2/channel-avatar @state]]]])))

(defn preview-channel-avatar []
  [rn/view {:background-color (colors/theme-colors colors/white
                                                   colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
