(ns status-im.ui2.screens.chat.components.chat-bottom-sheet.view
  (:require [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.constants :as constants]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.utils.universal-links.utils :as universal-links]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.screens.chat.styles.message.sheets :as sheets.styles]
            [quo.core :as quo]
            [status-im.chat.models.pin-message :as models.pin-message]
            [quo.react-native :as rn]
            [quo2.components.icon :as icons]
            [quo2.foundations.colors :as colors]
            [quo2.components.markdown.text :as text]
            [quo2.components.drawers.action-drawers :refer [divider]]
            [quo2.components.list-items.menu-item :as quo2.menu-item]))

(defn chat-bottom-sheet []
  [rn/view
   [quo2.menu-item/menu-item
    {:type                :main
     :title               (i18n/label :t/view-profile)
     :accessibility-label "view-profile"
     :icon                :friend
     :on-press             #(println "PPP")}]
   [divider]
   [quo2.menu-item/menu-item
    {:type                :main
     :title               (i18n/label :t/mark-as-read)
     :accessibility-label "mark-as-read"
     :icon                :correct
     :on-press             #(println "PPP")}]
   [quo2.menu-item/menu-item
    {:type                :main
     :title               (i18n/label :t/mute-chat)
     :description         "Muted for 15 minutes"
     :accessibility-label "mute-chat"
     :icon                :muted
     :arrow? true
     :on-press             #(println "PPP")}]
   [quo2.menu-item/menu-item
    {:type                :main
     :title               (i18n/label :t/notifications)
     :description         "Only @mentions"
     :accessibility-label "notifications"
     :icon                :notifications
     :arrow? true
     :on-press             #(println "PPP")}]
   [divider]
   [quo2.menu-item/menu-item
    {:type                :danger
     :title               (i18n/label :t/clear-history)
     :accessibility-label "clear-history"
     :icon                :delete
     :on-press             #(println "PPP")}]
   [quo2.menu-item/menu-item
    {:type                :danger
     :title               (i18n/label :t/delete-chat)
     :accessibility-label "delete-chat"
     :icon                :delete
     :on-press             #(println "PPP")}]
   ])
