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
            [status-im.utils.platform :refer [platform-specific]]))

(defview menu-item-icon-profile []
  [chat-id [:chat :chat-id]
   group-chat [:chat :group-chat]
   name [:chat :name]
   color [:chat :color]]
  ;; TODO stub data ('online' property)
  [chat-icon-view-menu-item chat-id group-chat name color true])

(defn- members-text [members]
  (str (s/join ", " (map :name members))
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

(defn group-chat-items [members public?]
  (into (if public? [] [(item-members members)])
        [item-search
         item-notifications
         item-settings]))

(defn user-chat-items [chat-id]
  [(item-user chat-id)
   item-search
   item-notifications])

(defn overlay [{:keys [on-click-outside]} items]
  [view st/actions-overlay
   [touchable-highlight {:on-press on-click-outside
                         :style    st/overlay-highlight}
    [view nil]]
   items])

(defn action-view [{:keys     [icon-style
                               custom-icon
                               handler
                               title
                               subtitle]
                    icon-name :icon}]
  [touchable-highlight {:on-press (fn []
                                    (dispatch [:set-chat-ui-props :show-actions? false])
                                    (when handler
                                      (handler)))}
   [view st/action-icon-row
    [view st/action-icon-view
     (or custom-icon
         [icon icon-name icon-style])]
    [view st/action-view
     [text {:style           st/action-title
            :number-of-lines 1
            :font            :medium}
      title]
     (when-let [subtitle subtitle]
       [text {:style           st/action-subtitle
              :number-of-lines 1
              :font            :default}
        subtitle])]]])

(defn actions-list-view []
  (let [{:keys [group-chat chat-id public?]}
        (subscribe [:chat-properties [:group-chat :chat-id :public?]])
        members (subscribe [:current-chat-contacts])
        status-bar-height (get-in platform-specific [:component-styles :status-bar :default :height])]
    (fn []
      (when-let [actions (if @group-chat
                           (group-chat-items @members @public?)
                           (user-chat-items @chat-id))]
        [view (merge
                (st/actions-wrapper status-bar-height)
                (get-in platform-specific [:component-styles :actions-list-view]))
         [view st/actions-separator]
         [view st/actions-view
          (for [action actions]
            (if action
              ^{:key action} [action-view action]))]]))))

(defn actions-view []
  [overlay {:on-click-outside #(dispatch [:set-chat-ui-props :show-actions? false])}
   [actions-list-view]])
