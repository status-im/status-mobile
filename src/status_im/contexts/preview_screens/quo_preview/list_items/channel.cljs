(ns status-im.contexts.preview-screens.quo-preview.list-items.channel
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key  :name
    :type :text}
   {:key     :notification
    :type    :select
    :options [{:key   nil
               :value "None"}
              {:key   :notification
               :value :notification}
              {:key   :mute
               :value :mute}
              {:key   :mention
               :value :mention}]}
   {:key  :mentions-count
    :type :text}
   {:key  :emoji
    :type :text}
   (preview/customization-color-option)
   {:key     :locked?
    :type    :select
    :options [{:key   nil
               :value "None"}
              {:key   false
               :value "Unlocked"}
              {:key   true
               :value "Locked"}]}])

(defn view
  []
  (let [state (reagent/atom {:name                "channel"
                             :notification        nil
                             :mentions-count      "5"
                             :emoji               "🍑"
                             :customization-color :blue
                             :locked?             nil})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [quo/channel @state]])))
