(ns status-im2.contexts.quo-preview.community.community-membership-list-view
  (:require [quo.previews.preview :as preview]
            [quo.react-native :as rn]
            [quo2.components.community.community-list-view :as community-list-view]
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
       [rn/view {:padding-bottom 150}
        [rn/view
         {:flex    1
          :padding 16}
         [preview/customizer state descriptor]]
        [rn/view
         {:padding-vertical 60
          :justify-content  :center}
         [community-list-view/communities-membership-list-item {}
          (cond-> (merge @state data/community)
            (= :muted (:notifications @state))
            (assoc :muted? true)

            (= :unread-mentions-count (:notifications @state))
            (assoc :unread-mentions-count 5)

            (= :unread-messages-count (:notifications @state))
            (assoc :unread-messages? true))]]]])))

(defn preview-community-list-view
  []
  [rn/view
   {:background-color (colors/theme-colors colors/neutral-5
                                           colors/neutral-95)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
