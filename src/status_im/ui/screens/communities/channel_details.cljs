(ns status-im.ui.screens.communities.channel-details
  (:require [quo.core :as quo]
            [status-im.utils.handlers :refer [>evt <sub]]
            [status-im.ui.components.profile-header.view :as profile-header]
            [status-im.i18n.i18n :as i18n]
            [clojure.string :as string]
            [status-im.communities.core :as communities]
            [re-frame.core :as re-frame]))

(defn view []
  (let [{:keys [chat-id]} (<sub [:get-screen-params])]
    (fn []
      (let [current-chat (<sub [:chat-by-id chat-id])
            {:keys [chat-name color description community-id emoji]} current-chat
            category (<sub [:chats/category-by-chat-id community-id chat-id])
            {:keys [admin]} (<sub [:communities/community community-id])
            pinned-messages (<sub [:chats/pinned chat-id])]
        [quo/animated-header {:left-accessories  [{:icon                :main-icons/arrow-left
                                                   :accessibility-label :back-button
                                                   :on-press            #(>evt [:navigate-back])}]
                              :right-accessories (when admin [{:icon                :edit
                                                               :accessibility-label :invite-button
                                                               :on-press            #(>evt [::communities/edit-channel-pressed
                                                                                            community-id
                                                                                            chat-name
                                                                                            description
                                                                                            color
                                                                                            emoji
                                                                                            chat-id])}])
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
              [quo/list-item {:title          (i18n/label :t/category)
                              :on-press       #(>evt [:open-modal :select-category {:chat current-chat
                                                                                    :category category
                                                                                    :community-id community-id}])
                              :chevron        true
                              :accessory      :text
                              :accessory-text (if category
                                                (:name category)
                                                (i18n/label :t/none))}])
            [quo/list-item
             {:title               (i18n/label :t/pinned-messages)
              :accessory           :text
              :accessory-text      (count pinned-messages)
              :chevron             true
              :on-press            #(re-frame/dispatch [:contact.ui/pinned-messages-pressed chat-id])}]])]))))
