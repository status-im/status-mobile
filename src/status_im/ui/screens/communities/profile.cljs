(ns status-im.ui.screens.communities.profile
  (:require [quo.core :as quo]
            [status-im.utils.handlers :refer [>evt <sub]]
            [status-im.ui.components.profile-header.view :as profile-header]
            [status-im.i18n :as i18n]
            [status-im.communities.core :as communities]
            [status-im.ui.components.colors :as colors]))

(defn management [route]
  (let [{:keys [community-id]}   (get-in route [:route :params])
        {:keys [id description]} (<sub [:communities/community community-id])
        roles                    false
        notifications            false
        members                  (count (get description :members))]
    [:<>
     [quo/animated-header {:left-accessories  [{:icon                :main-icons/arrow-left
                                                :accessibility-label :back-button
                                                :on-press            #(>evt [:navigate-back])}]
                           :right-accessories [{:icon                :main-icons/share
                                                :accessibility-label :invite-button
                                                :on-press            #(>evt [::communities/invite-people-pressed id])}]
                           :extended-header   (profile-header/extended-header
                                               {:title    (get-in description [:identity :display-name])
                                                :color    (get-in description [:identity :color] (rand-nth colors/chat-colors))
                                                :subtitle (i18n/label-pluralize members :t/community-members {:count members})})
                           :insets            false}
      [:<>
       [quo/list-footer {:color :main}
        (get-in description [:identity :description])]
       [quo/separator {:style {:margin-vertical 8}}]
       [quo/list-item {:chevron        true
                       :accessory-text (str members)
                       :on-press       #(>evt [:navigate-to :community-members {:community-id id}])
                       :title          (i18n/label :t/members-label)
                       :icon           :main-icons/group-chat}]
       (when roles
         [quo/list-item {:chevron true
                         :title   (i18n/label :t/commonuity-role)
                         :icon    :main-icons/objects}])
       (when notifications
         [quo/list-item {:chevron true
                         :title   (i18n/label :t/chat-notification-preferences)
                         :icon    :main-icons/notification}])
       [quo/separator {:style {:margin-vertical 8}}]
       [quo/list-item {:theme    :accent
                       :icon     :main-icons/edit
                       :title    "Edit community"
                       :on-press #(>evt [::communities/open-edit-community community-id])}]
       [quo/list-item {:theme :accent
                       :icon  :main-icons/arrow-left
                       :title "Leave community"}]
       [quo/list-item {:theme :negative
                       :icon  :main-icons/delete
                       :title "Delete"}]]]]))



