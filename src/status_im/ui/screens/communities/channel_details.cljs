(ns status-im.ui.screens.communities.channel-details
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [re-frame.core :as re-frame]
            [status-im.communities.core :as communities]
            [utils.i18n :as i18n]
            [status-im.ui.components.profile-header.view :as profile-header]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [{:keys [chat-id]} (rf/sub [:get-screen-params])]
    (fn []
      (let [current-chat                                             (rf/sub [:chat-by-id chat-id])
            {:keys [chat-name color description community-id emoji]} current-chat
            {:keys [position]}                                       (rf/sub [:chats/community-chat-by-id
                                                                              community-id chat-id])
            category                                                 (rf/sub [:chats/category-by-chat-id
                                                                              community-id chat-id])
            {:keys [admin]}                                          (rf/sub [:communities/community
                                                                              community-id])
            pinned-messages                                          (rf/sub [:chats/pinned chat-id])]
        [quo/animated-header
         {:left-accessories  [{:icon                :main-icons/arrow-left
                               :accessibility-label :back-button
                               :on-press            #(rf/dispatch [:navigate-back])}]
          :right-accessories (when admin
                               [{:icon                :edit
                                 :accessibility-label :invite-button
                                 :on-press            #(rf/dispatch [::communities/edit-channel-pressed
                                                                     community-id
                                                                     chat-name
                                                                     description
                                                                     color
                                                                     emoji
                                                                     chat-id
                                                                     (:id category)
                                                                     position])}])
          :extended-header   (profile-header/extended-header
                              {:title    chat-name
                               :color    color
                               :emoji    emoji
                               :subtitle (i18n/label :t/public-channel)})
          :use-insets        true}
         (when-not (string/blank? description)
           [:<>
            [quo/list-footer {:color :main}
             description]
            [quo/separator {:style {:margin-vertical 8}}]
            (when admin
              [quo/list-item
               {:title          (i18n/label :t/category)
                :on-press       #(rf/dispatch [:open-modal :select-category
                                               {:chat         current-chat
                                                :category     category
                                                :community-id community-id}])
                :chevron        true
                :accessory      :text
                :accessory-text (if category
                                  (:name category)
                                  (i18n/label :t/none))}])
            [quo/list-item
             {:title          (i18n/label :t/pinned-messages)
              :accessory      :text
              :accessory-text (count pinned-messages)
              :chevron        true
              :on-press       #(re-frame/dispatch [:chat.ui/navigate-to-pinned-messages
                                                   chat-id])}]])]))))
