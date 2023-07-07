(ns status-im2.contexts.quo-preview.avatars.channel-avatar
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Big?"
    :key   :big?
    :type  :boolean}
   {:label "Emoji"
    :key   :emoji
    :type  :text}
   {:label "Full name"
    :key   :full-name
    :type  :text}
   (preview/customization-color-option)
   {:label   "Is Locked?"
    :key     :locked?
    :type    :select
    :options [{:key   :not-set
               :value "None"}
              {:key   :unlocked
               :value "Unlocked"}
              {:key   :locked
               :value "Locked"}]}])

(defn cool-preview
  []
  (let [state (reagent/atom {:big?      true
                             :locked?   :not-set
                             :emoji     "🍑"
                             :full-name "Some channel"
                             :color     :blue})]
    (fn []
      (let [color   (colors/custom-color-by-theme (:color @state) 50 60)
            locked? (case (:locked? @state)
                      :not-set  nil
                      :unlocked false
                      :locked   true
                      nil)]
        [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
         [rn/view {:style {:padding-bottom 150}}
          [rn/view {:style {:flex 1}}
           [preview/customizer state descriptor]]
          [rn/view
           {:style {:padding-vertical 60
                    :flex-direction   :row
                    :justify-content  :center}}
           [quo/channel-avatar
            (assoc @state
                   :locked? locked?
                   :color   color)]]]]))))

(defn preview-channel-avatar
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
