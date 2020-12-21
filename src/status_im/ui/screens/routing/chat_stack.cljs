(ns status-im.ui.screens.routing.chat-stack
  (:require [status-im.ui.screens.routing.core :as navigation]
            [status-im.ui.screens.home.views :as home]
            [status-im.ui.screens.chat.views :as chat]
            [status-im.ui.screens.group.views :as group]
            [status-im.ui.screens.referrals.public-chat :as referrals.public-chat]
            [status-im.ui.screens.communities.views :as communities]
            [status-im.ui.screens.communities.community :as community]
            [status-im.ui.screens.communities.create :as communities.create]
            [status-im.ui.screens.communities.import :as communities.import]
            [status-im.ui.screens.communities.profile :as community.profile]
            [status-im.ui.screens.communities.edit :as community.edit]
            [status-im.ui.screens.communities.create-channel :as create-channel]
            [status-im.ui.screens.communities.membership :as membership]
            [status-im.ui.screens.communities.members :as members]
            [status-im.ui.screens.communities.requests-to-join :as requests-to-join]
            [status-im.ui.screens.communities.invite :as invite]
            [status-im.ui.screens.profile.group-chat.views :as profile.group-chat]
            [status-im.ui.components.tabbar.styles :as tabbar.styles]
            [status-im.ui.screens.stickers.views :as stickers]
            [status-im.utils.config :as config]))

(defonce stack (navigation/create-stack))
(defonce group-stack (navigation/create-stack))
(defonce communities-stack (navigation/create-stack))

(defn chat-stack []
  [stack {:initial-route-name :home
          :header-mode        :none}
   [{:name      :home
     :style     {:padding-bottom tabbar.styles/tabs-diff}
     :component home/home}
    {:name      :referral-enclav
     :component referrals.public-chat/view}
    {:name      :chat
     :component chat/chat}
    {:name      :group-chat-profile
     :insets    {:top false}
     :component profile.group-chat/group-chat-profile}
    {:name      :group-chat-invite
     :component profile.group-chat/group-chat-invite}
    {:name      :stickers
     :component stickers/packs}
    {:name      :stickers-pack
     :component stickers/pack}
    ;; Community
    {:name      :community
     :component community/community}
    {:name      :community-management
     :insets    {:top false}
     :component community.profile/management-container}
    {:name      :community-members
     :component members/members-container}
    {:name      :community-requests-to-join
     :component requests-to-join/requests-to-join-container}
    {:name      :create-community-channel
     :component create-channel/create-channel}
    {:name      :invite-people-community
     :component invite/invite}]])

(defn communities []
  [communities-stack {:header-mode :none}
   (concat
    [{:name      :communities
      :insets    {:bottom true
                  :top    false}
      :component communities/communities}
     {:name      :community-import
      :insets    {:bottom true
                  :top    false}
      :component communities.import/view}
     {:name      :invite-people-community
      :insets    {:bottom true
                  :top    false}
      :component invite/invite}]
    (when config/communities-management-enabled?
      [{:name      :community-edit
        :insets    {:bottom true
                    :top    false}
        :component community.edit/edit}
       {:name      :community-create
        :insets    {:bottom true
                    :top    false}
        :component communities.create/view}
       {:name      :community-membership
        :insets    {:bottom true
                    :top    false}
        :component membership/membership}]))])

(defn new-group-chat []
  [group-stack {:header-mode        :none
                :initial-route-name :contact-toggle-list}
   [{:name      :contact-toggle-list
     :insets    {:top    false
                 :bottom true}
     :component group/contact-toggle-list}
    {:name      :new-group
     :insets    {:top    false
                 :bottom true}
     :component group/new-group}]])
