(ns status-im.ui.screens.routing.chat-stack
  (:require [status-im.ui.screens.routing.core :as navigation]
            [status-im.ui.screens.home.views :as home]
            [status-im.ui.screens.chat.views :as chat]
            [status-im.ui.screens.profile.contact.views :as profile.contact]
            [status-im.ui.screens.group.views :as group]
            [status-im.ui.screens.profile.group-chat.views :as profile.group-chat]
            [status-im.chat.models.loading :as chat.loading]
            [status-im.ui.screens.group.events :as group.events]
            [status-im.ui.screens.stickers.views :as stickers]))

(defonce stack (navigation/create-stack))

(defn chat-stack []
  [stack {:initial-route-name :home
          :header-mode        :none}
   [{:name      :home
     :on-focus  [::chat.loading/offload-all-messages]
     :component home/home}
    {:name      :chat
     :on-focus  [::chat.loading/load-messages]
     :component chat/chat}
    {:name      :profile
     :component profile.contact/profile}
    {:name      :new-group
     :component group/new-group}
    {:name      :add-participants-toggle-list
     :on-focus  [::group.events/add-participants-toggle-list]
     :component group/add-participants-toggle-list}
    {:name      :contact-toggle-list
     :component group/contact-toggle-list}
    {:name      :group-chat-profile
     :component profile.group-chat/group-chat-profile}
    {:name      :stickers
     :component stickers/packs}
    {:name      :stickers-pack
     :component stickers/pack}]])
