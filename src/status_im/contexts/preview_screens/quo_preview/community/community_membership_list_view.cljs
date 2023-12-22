(ns status-im.contexts.preview-screens.quo-preview.community.community-membership-list-view
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.community.data :as data]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :notifications
    :type    :select
    :options [{:key :muted}
              {:key :unread-mentions-count}
              {:key :unread-messages-count}]}
   {:key     :status
    :type    :select
    :options [{:key :gated}
              {:key :open}]}
   {:key  :locked?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:locked?       true
                             :notifications :muted
                             :status        :gated})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/communities-membership-list-item {}
        false
        (cond-> (merge @state data/community)
          (= :muted (:notifications @state))
          (assoc :muted? true)

          (= :unread-mentions-count (:notifications @state))
          (assoc :unread-mentions-count 5)

          (= :unread-messages-count (:notifications @state))
          (assoc :unread-messages? true))]])))
