(ns status-im2.contexts.quo-preview.list-items.channel
  (:require [quo2.components.list-items.channel :as quo2-channel]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Muted?:"
    :key   :muted?
    :type  :boolean}
   {:label "Name"
    :key   :name
    :type  :text}
   {:label "Number of mentions"
    :key   :mentions-count
    :type  :text}
   {:label "Unread Messages:"
    :key   :unread-messages?
    :type  :boolean}
   {:label "Avatar emoji"
    :key   :emoji
    :type  :text}
   {:label   "is Locked?"
    :key     :locked?
    :type    :select
    :options [{:key   nil
               :value "None"}
              {:key   false
               :value "Unlocked"}
              {:key   true
               :value "Locked"}]}
   {:label "Is Pressed Channel:"
    :key   :is-active-channel?
    :type  :boolean}
   {:label   "Channel color"
    :key     :channel-color
    :type    :select
    :options [{:key   "#00FFFF"
               :value "Blue"}
              {:key   "#FF00FF"
               :value "Pink"}
              {:key   "#FFFF00"
               :value "Yellow"}]}])

(defn cool-preview
  []
  (let [state (reagent/atom {:is-active-channel? false
                             :muted?             false
                             :unread-messages?   false
                             :emoji              "🍑"
                             :channel-color      "#4360DF"
                             :mentions-count     "5"
                             :name               "channel"
                             :locked?            true})]
    (fn []
      [rn/view
       {:margin-bottom  50
        :padding-bottom 16
        :padding-right  8
        :padding-left   8

        :padding-top    16}
       [preview/customizer state descriptor]
       [rn/view
        {:padding-vertical 60
         :align-items      :center}
        [quo2-channel/list-item @state]]])))

(defn preview-channel
  []
  [rn/keyboard-avoiding-view {:style {:flex 1}}
   [rn/view
    {:background-color (colors/theme-colors colors/white colors/neutral-90)
     :flex             1}
    [rn/flat-list
     {:flex                         1
      :keyboard-should-persist-taps :always
      :header                       [cool-preview]
      :key-fn                       str}]]])
