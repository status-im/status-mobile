(ns status-im2.contexts.quo-preview.list-items.community-list
  (:require [quo.previews.preview :as preview]
            [quo.react-native :as rn]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.community.data :as data]))

(def descriptor
  [{:label   "Notifications:"
    :key     :notifications
    :type    :select
    :options [{:key   :muted
               :value "Muted"}
              {:key   :unread-mentions-count
               :value "Mention counts"}
              {:key   :unread-messages-count
               :value "Unread messages"}]}
   {:label   "Status:"
    :key     :status
    :type    :select
    :options [{:key   :gated
               :value "Gated"}
              {:key   :open
               :value "Open"}]}
   {:label "Locked:"
    :key   :locked?
    :type  :boolean}])

(defn cool-preview
  []
  (let [notifications (reagent/atom (:notifications nil))
        state         (reagent/atom {:locked?       true
                                     :notifications nil
                                     :status        (if notifications
                                                      :gated
                                                      :open)})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:style {:margin-bottom 20}}
        [preview/customizer state descriptor]
        [rn/view {:style {:margin-top 20}}
         [quo/community-list {}
          (cond-> (merge @state data/community)
            (= :muted (:notifications @state))
            (assoc :muted? true)

            (= :unread-mentions-count (:notifications @state))
            (assoc :unread-mentions-count 5)

            (= :unread-messages-count (:notifications @state))
            (assoc :unread-messages? true))]]]])))

(defn preview
  []
  [rn/view
   {:background-color (colors/theme-colors colors/neutral-5 colors/neutral-95)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
