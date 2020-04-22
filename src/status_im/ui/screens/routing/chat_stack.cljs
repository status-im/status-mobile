(ns status-im.ui.screens.routing.chat-stack
  (:require [status-im.ui.screens.routing.core :as navigation]
            [status-im.ui.screens.home.views :as home]
            [status-im.ui.screens.chat.views :as chat]
            [status-im.ui.screens.profile.contact.views :as profile.contact]
            [status-im.ui.screens.group.views :as group]
            [status-im.ui.screens.profile.group-chat.views :as profile.group-chat]
            [status-im.chat.models.loading :as chat.loading]
            [status-im.ui.screens.group.events :as group.events]
            [status-im.chat.models :as chat.models]
            [status-im.ui.components.tabbar.styles :as tabbar.styles]
            [status-im.ui.screens.stickers.views :as stickers]))

(defonce stack (navigation/create-stack))
(defonce group-stack (navigation/create-stack))

(defn chat-stack []
  [stack {:initial-route-name :home
          :header-mode        :none}
   [{:name      :home
     :on-focus  [::chat.models/offload-all-messages]
     :style     {:padding-bottom tabbar.styles/tabs-diff}
     :component home/home}
    {:name      :chat
     :component chat/chat}
    {:name      :profile
     :component profile.contact/profile}
    {:name      :group-chat-profile
     :component profile.group-chat/group-chat-profile}
    {:name      :stickers
     :component stickers/packs}
    {:name      :stickers-pack
     :component stickers/pack}]])

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
