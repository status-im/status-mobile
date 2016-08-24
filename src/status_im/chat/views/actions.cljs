(ns status-im.chat.views.actions
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as s]
            [status-im.components.react :refer [view
                                                animated-view
                                                text
                                                icon
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.components.chat-icon.screen :refer [chat-icon-view-menu-item]]
            [status-im.chat.styles.screen :as st]
            [status-im.i18n :refer [label label-pluralize]]
            [status-im.utils.logging :as log]))

(defview menu-item-icon-profile []
  [chat-id [:chat :chat-id]
   group-chat [:chat :group-chat]
   name [:chat :name]
   color [:chat :color]]
  ;; TODO stub data ('online' property)
  [chat-icon-view-menu-item chat-id group-chat name color true])

(defn- members-text [members]
  (str (s/join ", " (map #(:name %) members))
       " "
       (label :t/and-you)))

(defn item-members [members]
  {:title      (label :t/members-title)
   :subtitle   (members-text members)
   :icon       :menu_group
   :icon-style {:width  25
                :height 19}
   ;; TODO not implemented: action Members
   :handler    nil})

(defn item-user [chat-id]
  {:title       (label :t/profile)
   :custom-icon [menu-item-icon-profile]
   :icon        :menu_group
   :icon-style  {:width  25
                 :height 19}
   :handler     #(dispatch [:show-profile chat-id])})

(defn item-add-to-contacts [contact]
  {:title      (label :t/add-to-contacts)
   :icon       :menu_group
   :icon-style {:width  20
                :height 17}
   :handler    #(dispatch [:add-pending-contact contact])})

(def item-search
  {:title      (label :t/search-chat)
   :subtitle   (label :t/not-implemented)
   :icon       :search_gray_copy
   :icon-style {:width  17
                :height 17}
   ;; TODO not implemented: action Search chat
   :handler    nil})

(def item-notifications
  {:title      (label :t/notifications-title)
   :subtitle   (label :t/not-implemented)
   ;;:subtitle   "Chat muted"
   :icon       :muted
   :icon-style {:width  18
                :height 21}
   ;; TODO not implemented: action Notifications
   :handler    nil})

(def item-settings
  {:title      (label :t/settings)
   :icon       :settings
   :icon-style {:width  20
                :height 13}
   :handler    #(dispatch [:show-group-settings])})

(defn group-chat-items [members]
  [(item-members members)
   item-search
   item-notifications
   item-settings])

(defn user-chat-items [chat-id {:keys [pending] :as contact}]
  [(item-user chat-id)
   (if pending (item-add-to-contacts contact) nil)
   item-search
   item-notifications
   item-settings])

(defn overlay [{:keys [on-click-outside]} items]
  [view st/actions-overlay
   [touchable-highlight {:on-press on-click-outside
                         :style    st/overlay-highlight}
    [view nil]]
   items])

(defn action-view [{{:keys     [icon-style
                                custom-icon
                                handler
                                title
                                subtitle]
                     icon-name :icon} :action
                    platform-specific :platform-specific}]
  [touchable-highlight {:on-press (fn []
                                    (dispatch [:set-show-actions false])
                                    (when handler
                                      (handler)))}
   [view st/action-icon-row
    [view st/action-icon-view
     (or custom-icon
         [icon icon-name icon-style])]
    [view st/action-view
     [text {:style             st/action-title
            :platform-specific platform-specific
            :number-of-lines   1
            :font              :medium} title]
     (when-let [subtitle subtitle]
       [text {:style             st/action-subtitle
              :platform-specific platform-specific
              :number-of-lines   1
              :font              :default}
        subtitle])]]])

(defn actions-list-view [{styles :styles :as platform-specific}]
  (let [{:keys [group-chat chat-id]} (subscribe [:chat-properties [:group-chat :chat-id]])
        members (subscribe [:current-chat-contacts])
        status-bar-height (get-in styles [:components :status-bar :default :height])]
    (when-let [actions (if @group-chat
                         (group-chat-items @members)
                         (user-chat-items @chat-id (first @members)))]
      [view (-> (st/actions-wrapper status-bar-height)
                (merge (get-in styles [:components :actions-list-view])))
       [view st/actions-separator]
       [view st/actions-view
        (for [action actions]
          (if action
            ^{:key action} [action-view {:platform-specific platform-specific
                                         :action            action}]))]])))

(defn actions-view [platform-specific]
  [overlay {:on-click-outside #(dispatch [:set-show-actions false])}
   [actions-list-view platform-specific]])