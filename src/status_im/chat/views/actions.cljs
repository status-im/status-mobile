(ns status-im.chat.views.actions
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.chat.styles.screen :as styles]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.react :as react]
            [status-im.i18n :as i18n]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defview menu-item-icon-profile []
  [chat-id [:chat :chat-id]
   group-chat [:chat :group-chat]
   name [:chat :name]
   color [:chat :color]]
  ;; TODO stub data ('online' property)
  [chat-icon.screen/chat-icon-view-menu-item chat-id group-chat name color true])

(defn- members-text [members]
  (str (string/join ", " (map :name members))
       " "
       (i18n/label :t/and-you)))

(defn item-members [members]
  {:title      (i18n/label :t/members-title)
   :subtitle   (members-text members)
   :icon       :menu_group
   :icon-opts {:width  25
               :height 19}
   ;; TODO not implemented: action Members
   :handler    nil})

(defn item-user [chat-id]
  {:title       (i18n/label :t/profile)
   :custom-icon [menu-item-icon-profile]
   :icon        :menu_group
   :icon-style  {:width  25
                 :height 19}
   :handler     #(re-frame/dispatch [:show-profile chat-id])})

(defn item-delete [chat-id]
  {:title      (i18n/label :t/delete-chat)
   :icon       :search_gray_copy
   :icon-style {:width  17
                :height 17}
   ;; TODO(jeluard) Refactor this or Jan will have an heart attack
   :handler    #(do (re-frame/dispatch [:remove-chat chat-id])
                    (re-frame/dispatch [:navigation-replace :home]))})

(def item-notifications
  {:title      (i18n/label :t/notifications-title)
   :subtitle   (i18n/label :t/not-implemented)
   ;;:subtitle   "Chat muted"
   :icon       :muted
   :icon-style {:width  18
                :height 21}
   ;; TODO not implemented: action Notifications
   :handler    nil})

(def item-settings
  {:title               (i18n/label :t/settings)
   :icon                :settings
   :icon-style          {:width  20
                         :height 13}
   :accessibility-label :settings
   :handler             #(re-frame/dispatch [:show-group-chat-settings])})

(defn group-chat-items [members public?]
  (into (if public? [] [(item-members members)])
        [item-notifications
         item-settings]))

(defn user-chat-items [chat-id]
  [(item-user chat-id)
   (item-delete chat-id)
   item-notifications])

(defn overlay [{:keys [on-click-outside]} items]
  [react/view styles/actions-overlay
   [react/touchable-highlight {:on-press on-click-outside
                               :style    styles/overlay-highlight}
    [react/view nil]]
   items])

(defn action-view [{:keys     [react/icon-style
                               icon
                               custom-icon
                               handler
                               title
                               subtitle
                               accessibility-label]
                    :or       {accessibility-label :action}}]
  [react/touchable-highlight {:on-press (fn []
                                          (re-frame/dispatch [:set-chat-ui-props {:show-actions? false}])
                                          (when handler
                                            (handler)))}
   [react/view {:accessibility-label accessibility-label
                :style styles/action-icon-row}
    [react/view styles/action-icon-view
     (or custom-icon
         [react/icon icon icon-style])]
    [react/view styles/action-view
     [react/text {:style           styles/action-title
                  :number-of-lines 1
                  :font            :medium}
      title]
     (when-let [subtitle subtitle]
       [react/text {:style           styles/action-subtitle
                    :number-of-lines 1
                    :font            :default}
        subtitle])]]])

(defview actions-list-view []
  (letsubs [group-chat        [:chat :group-chat]
            chat-id           [:chat :chat-id]
            public?           [:chat :public?]
            members           [:current-chat-contacts]
            status-bar-height (get platform/platform-specific :status-bar-default-height)]
    (when-let [actions (if group-chat
                         (group-chat-items members public?)
                         (user-chat-items chat-id))]
      [react/view (merge
                   (styles/actions-wrapper status-bar-height)
                   styles/actions-list-view)
       [react/view styles/actions-separator]
       [react/view styles/actions-view
        (for [action actions]
          (if action
            ^{:key action} [action-view action]))]])))

(defn actions-view []
  [overlay {:on-click-outside #(re-frame/dispatch [:set-chat-ui-props {:show-actions? false}])}
   [actions-list-view]])
