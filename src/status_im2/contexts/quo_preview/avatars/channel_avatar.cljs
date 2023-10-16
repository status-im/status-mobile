(ns status-im2.contexts.quo-preview.avatars.channel-avatar
  (:require
    [quo2.core :as quo]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :size
    :type    :select
    :options [{:key :size-64}
              {:key :size-32}
              {:key   nil
               :value "Default"}]}
   {:key  :emoji
    :type :text}
   {:key  :full-name
    :type :text}
   (preview/customization-color-option)
   {:key     :badge
    :type    :select
    :options [{:key   nil
               :value "Default"}
              {:key :unlocked}
              {:key :locked}]}])

(defn view
  []
  (let [state (reagent/atom {:size                :size-32
                             :badge               nil
                             :emoji               "🍑"
                             :full-name           "Some channel"
                             :customization-color :blue})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/channel-avatar @state]])))
