(ns status-im2.contexts.quo-preview.list-items.channel
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key  :muted?
    :type :boolean}
   {:key  :name
    :type :text}
   {:key  :mentions-count
    :type :text}
   {:key  :unread-messages?
    :type :boolean}
   {:key  :emoji
    :type :text}
   {:key     :locked?
    :type    :select
    :options [{:key   nil
               :value "None"}
              {:key   false
               :value "Unlocked"}
              {:key   true
               :value "Locked"}]}
   {:key  :is-active-channel?
    :type :boolean}
   {:key     :channel-color
    :type    :select
    :options [{:key   "#00FFFF"
               :value "Blue"}
              {:key   "#FF00FF"
               :value "Pink"}
              {:key   "#FFFF00"
               :value "Yellow"}]}])

(defn view
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
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/channel-list-item @state]])))
