(ns status-im2.contexts.quo-preview.list-items.channel
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [quo2.foundations.colors :as colors]))

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
      (let [customization-color (colors/custom-color-by-theme (:customization-color @state) 50 60)]
        [preview/preview-container
         {:state      state
          :descriptor descriptor}
         [quo/channel (assoc @state :customization-color customization-color)]]))))
