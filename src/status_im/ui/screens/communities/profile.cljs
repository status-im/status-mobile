(ns status-im.ui.screens.communities.profile
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [quo.react-native :as rn]
            [reagent.core :as reagent]
            [status-im.communities.core :as communities]
            [status-im2.setup.constants :as constants]
            [i18n.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.copyable-text :as copyable-text]
            [status-im.ui.components.profile-header.view :as profile-header]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.unviewed-indicator :as unviewed-indicator]
            [utils.re-frame :as rf]))

(defn management
  []
  (let [{:keys [community-id]} (rf/sub [:get-screen-params])]
    (fn []
      (let [requests-to-join (rf/sub [:communities/requests-to-join-for-community community-id])
            community (rf/sub [:communities/community community-id])
            {:keys [color members permissions description name admin]} community
            roles false
            notifications false
            show-members-count? (not= (:access permissions) constants/community-no-membership-access)
            request-membership? (= (:access permissions) constants/community-on-request-access)
            members-count (count members)]
        [:<>
         [quo/animated-header
          {:left-accessories  [{:icon                :main-icons/arrow-left
                                :accessibility-label :back-button
                                :on-press            #(rf/dispatch [:navigate-back])}]
           :right-accessories [{:icon                :main-icons/share
                                :accessibility-label :invite-button
                                :on-press            #(rf/dispatch [:communities/share-community-pressed
                                                                    community-id])}]
           :extended-header   (profile-header/extended-header
                               {:title      name
                                :color      (or color (rand-nth colors/chat-colors))
                                :photo      (if (= community-id constants/status-community-id)
                                              (:uri
                                               (rn/resolve-asset-source
                                                (resources/get-image :status-logo)))
                                              (get-in community [:images :large :uri]))
                                :membership (when request-membership?
                                              (i18n/label :t/membership-approval))
                                :subtitle   (if show-members-count?
                                              (i18n/label-pluralize members-count
                                                                    :t/community-members
                                                                    {:count members-count})
                                              (i18n/label :t/open-membership))
                                :community? true})
           :use-insets        true}
          [:<>
           (when-not (string/blank? description)
             [:<>
              [quo/list-footer {:color :main}
               description]
              [quo/separator {:style {:margin-vertical 8}}]])
           [:<>
            (let [link (communities/universal-link community-id)]
              [react/view
               {:padding-vertical   10
                :padding-horizontal 16}
               [react/view {:margin-bottom 20}
                [quo/text {:color :secondary} (i18n/label :t/community-link)]]
               [copyable-text/copyable-text-view
                {:copied-text link}
                [react/view
                 {:border-radius      16
                  :padding-horizontal 16
                  :padding-vertical   11
                  :background-color   colors/blue-light}
                 [quo/text {:color :link} (subs link 8)]]]])
            [quo/separator {:style {:margin-vertical 8}}]]
           (when show-members-count?
             [quo/list-item
              {:chevron   true
               :accessory
               [react/view {:flex-direction :row}
                (when (pos? members-count)
                  [quo/text {:color :secondary} (str members-count)])
                [unviewed-indicator/unviewed-indicator (count requests-to-join)]]
               :on-press  #(rf/dispatch [:navigate-to :community-members {:community-id community-id}])
               :title     (i18n/label :t/members-label)
               :icon      :main-icons/group-chat}])
           (when (and admin roles)
             [quo/list-item
              {:chevron true
               :title   (i18n/label :t/commonuity-role)
               :icon    :main-icons/objects}])
           (when notifications
             [quo/list-item
              {:chevron true
               :title   (i18n/label :t/chat-notification-preferences)
               :icon    :main-icons/notification}])
           (when (or show-members-count? notifications (and admin roles))
             [quo/separator {:style {:margin-vertical 8}}])
           (when admin
             [quo/list-item
              {:theme    :accent
               :icon     :main-icons/edit
               :title    (i18n/label :t/edit-community)
               :on-press #(rf/dispatch [::communities/open-edit-community community-id])}])
           [quo/list-item
            {:theme    :accent
             :icon     :main-icons/arrow-left
             :title    (i18n/label :t/leave-community)
             :on-press #(rf/dispatch [:communities/leave community-id])}]
           ;; Disable as not implemented yet
           (when false
             [quo/list-item
              {:theme    :negative
               :icon     :main-icons/delete
               :title    (i18n/label :t/delete)
               :on-press #(rf/dispatch [::communities/delete-community community-id])}])]]]))))

(defn management-container
  []
  (reagent/create-class
   {:display-name        "community-profile-view"
    :component-did-mount (fn []
                           (communities/fetch-requests-to-join! (get (rf/sub [:get-screen-params])
                                                                     :community-id)))
    :reagent-render      management}))
