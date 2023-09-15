(ns status-im2.contexts.quo-preview.avatars.channel-avatar
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :size
    :type    :select
    :options [{:key :size-64}
              {:key :size-32}
              {:key :default}]}
   {:key  :emoji
    :type :text}
   {:key  :full-name
    :type :text}
   (preview/customization-color-option)
   {:key     :locked-state
    :type    :select
    :options [{:key :not-set}
              {:key :unlocked}
              {:key :locked}]}])

(defn view
  []
  (let [state (reagent/atom {:size                :size-32
                             :locked-state        :not-set
                             :emoji               "🍑"
                             :full-name           "Some channel"
                             :customization-color :blue})]
    (fn []
      (let [customization-color (colors/custom-color-by-theme (:customization-color @state) 50 60)
            locked?             (case (:locked-state @state)
                                  :not-set  nil
                                  :unlocked false
                                  :locked   true
                                  nil)]
        [preview/preview-container {:state state :descriptor descriptor}
         [quo/channel-avatar
          (assoc @state
                 :locked?             locked?
                 :customization-color customization-color)]]))))
