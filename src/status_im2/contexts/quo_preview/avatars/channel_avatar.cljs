(ns status-im2.contexts.quo-preview.avatars.channel-avatar
  (:require [clojure.string :as string]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            utils.number))

(def descriptor
  [{:label "Big?"
    :key   :big?
    :type  :boolean}
   {:label   "Custom color"
    :key     :color
    :type    :select
    :options (->> colors/customization
                  keys
                  sort
                  (map (fn [k]
                         {:key k :value (string/capitalize (name k))})))}
   {:label "Emoji"
    :key   :emoji
    :type  :text}
   {:label "Full name"
    :key   :full-name
    :type  :text}
   {:label "Number of initials"
    :key   :amount-initials
    :type  :text}
   {:label   "Is Locked?"
    :key     :locked?
    :type    :select
    :options [{:key   nil
               :value "None"}
              {:key   false
               :value "Unlocked"}
              {:key   true
               :value "Locked"}]}])

(defn cool-preview
  []
  (let [state (reagent/atom {:big?            true
                             :locked?         nil
                             :emoji           "🍑"
                             :full-name       "Some channel"
                             :amount-initials "1"
                             :color           :blue})]
    (fn []
      (let [amount-initials (utils.number/parse-int (:amount-initials @state) 1)
            color           (colors/custom-color-by-theme (:color @state) 50 60)]
        [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
         [rn/view {:padding-bottom 150}
          [rn/view {:flex 1}
           [preview/customizer state descriptor]]
          [rn/view
           {:padding-vertical 60
            :flex-direction   :row
            :justify-content  :center}
           [quo/channel-avatar
            (assoc @state
                   :amount-initials amount-initials
                   :color           color)]]]]))))

(defn preview-channel-avatar
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
