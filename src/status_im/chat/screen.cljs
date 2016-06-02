(ns status-im.chat.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as s]
            [status-im.components.react :refer [view
                                              text
                                              image
                                              icon
                                              touchable-highlight
                                              list-view
                                              list-item]]
            [status-im.components.chat-icon.screen :refer [chat-icon-view-action
                                                           chat-icon-view-menu-item]]
            [status-im.chat.styles.screen :as st]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.utils.utils :refer [truncate-str]]
            [status-im.components.invertible-scroll-view :refer [invertible-scroll-view]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.chat.views.message :refer [chat-message]]
            [status-im.chat.views.content-suggestions :refer [content-suggestions-view]]
            [status-im.chat.views.suggestions :refer [suggestions-view]]
            [status-im.chat.views.response :refer [request-view]]
            [status-im.chat.views.new-message :refer [chat-message-new]]))


(defn contacts-by-identity [contacts]
  (->> contacts
       (map (fn [{:keys [identity] :as contact}]
              [identity contact]))
       (into {})))

(defn add-msg-color [{:keys [from] :as msg} contact-by-identity]
  (if (= "system" from)
    (assoc msg :text-color :#4A5258
               :background-color :#D3EEEF)
    (let [{:keys [text-color background-color]} (get contact-by-identity from)]
      (assoc msg :text-color text-color
                 :background-color background-color))))

(defview chat-icon []
  [chat-id    [:chat :chat-id]
   group-chat [:chat :group-chat]
   name       [:chat :name]
   color      [:chat :color]]
  ;; TODO stub data ('online' property)
  [chat-icon-view-action chat-id group-chat name color true])

(defn typing [member]
  [view st/typing-view
   [view st/typing-background
    [text {:style st/typing-text}
     (str member " is typing")]]])

(defn typing-all []
  [view st/typing-all
   ;; TODO stub data
   (for [member ["Geoff" "Justas"]]
     ^{:key member} [typing member])])

(defn message-row [contact-by-identity group-chat]
  (fn [row _ idx]
    (let [msg (-> row
                  (add-msg-color contact-by-identity)
                  (assoc :group-chat group-chat)
                  (assoc :last-msg (zero? (js/parseInt idx))))]
      (list-item [chat-message msg]))))

(defn on-action-selected [position]
  (case position
    0 (dispatch [:navigate-to :add-participants])
    1 (dispatch [:navigate-to :remove-participants])
    2 (dispatch [:leave-group-chat])))

(defn overlay [{:keys [on-click-outside]} items]
  [view st/actions-overlay
   [touchable-highlight {:on-press on-click-outside
                         :style    st/overlay-highlight}
    [view nil]]
   items])

(defn action-view [{:keys     [icon-style custom-icon handler title subtitle]
                    icon-name :icon}]
  [touchable-highlight {:on-press (fn []
                                    (dispatch [:set-show-actions false])
                                    (when handler
                                      (handler)))}
   [view st/action-icon-row
    [view st/action-icon-view
     (or custom-icon
         [icon icon-name icon-style])]
    [view st/action-view
     [text {:style st/action-title} title]
     (when-let [subtitle subtitle]
       [text {:style st/action-subtitle}
        subtitle])]]])

(defview menu-item-icon-profile []
  [chat-id    [:chat :chat-id]
   group-chat [:chat :group-chat]
   name       [:chat :name]
   color      [:chat :color]]
  ;; TODO stub data ('online' property)
  [chat-icon-view-menu-item chat-id group-chat name color true])

(defn members-text [members]
  (truncate-str (str (s/join ", " (map #(:name %) members)) " and you") 35))

(defn actions-list-view []
  (let [{:keys [group-chat chat-id]}
        (subscribe [:chat-properties [:group-chat :chat-id]])
        members (subscribe [:current-chat-contacts])]
    (when-let [actions (if @group-chat
                         [{:title      "Members"
                           :subtitle   (members-text @members)
                           :icon       :menu_group
                           :icon-style {:width  25
                                        :height 19}
                           ;; TODO not implemented: action Members
                           :handler    nil}
                          {:title      "Search chat"
                           :subtitle   "!not implemented"
                           :icon       :search_gray_copy
                           :icon-style {:width  17
                                        :height 17}
                           ;; TODO not implemented: action Search chat
                           :handler    nil}
                          {:title      "Notifications and sounds"
                           :subtitle   "!not implemented"
                           ;;:subtitle   "Chat muted"
                           :icon       :muted
                           :icon-style {:width  18
                                        :height 21}
                           ;; TODO not implemented: action Notifications
                           :handler    nil}
                          {:title      "Settings"
                           :icon       :settings
                           :icon-style {:width  20
                                        :height 13}
                           :handler    #(dispatch [:show-group-settings])}]
                         [{:title      "Profile"
                           :custom-icon [menu-item-icon-profile]
                           :icon       :menu_group
                           :icon-style {:width  25
                                        :height 19}
                           :handler    #(dispatch [:show-profile @chat-id])}
                          {:title      "Search chat"
                           :subtitle   "!not implemented"
                           :icon       :search_gray_copy
                           :icon-style {:width  17
                                        :height 17}
                           ;; TODO not implemented: action Search chat
                           :handler    nil}
                          {:title      "Notifications and sounds"
                           :subtitle   "!not implemented"
                           ;;:subtitle   "Notifications on"
                           :icon       :muted
                           :icon-style {:width  18
                                        :height 21}
                           ;; TODO not implemented: action Notifications
                           :handler    nil}
                          {:title      "Settings"
                           :subtitle   "!not implemented"
                           :icon       :settings
                           :icon-style {:width  20
                                        :height 13}
                           ;; TODO not implemented: action Settings
                           :handler    nil}])]
      [view st/actions-wrapper
       [view st/actions-separator]
       [view st/actions-view
        (for [action actions]
          ^{:key action} [action-view action])]])))

(defn actions-view []
  [overlay {:on-click-outside #(dispatch [:set-show-actions false])}
   [actions-list-view]])

(defn toolbar-content []
  (let [{:keys [group-chat name contacts]}
        (subscribe [:chat-properties [:group-chat :name :contacts]])
        show-actions (subscribe [:show-actions])]
    (fn []
      [view (st/chat-name-view @show-actions)
       [text {:style st/chat-name-text}
        (truncate-str (or @name "Chat name") 30)]
       (if @group-chat
         [view {:flexDirection :row}
          [icon :group st/group-icon]
          [text {:style st/members}
           (let [cnt (inc (count @contacts))]
             (str cnt
                  (if (< 1 cnt)
                    " members"
                    " member")
                  ;; TODO stub data: active members
                  ", " cnt " active"))]]
         ;; TODO stub data: last activity
         [text {:style st/last-activity} "Active a minute ago"])])))

(defn toolbar-action []
  (let [show-actions (subscribe [:show-actions])]
    (fn []
      (if @show-actions
        [touchable-highlight
         {:on-press #(dispatch [:set-show-actions false])}
         [view st/action
          [icon :up st/up-icon]]]
        [touchable-highlight
         {:on-press #(dispatch [:set-show-actions true])}
         [view st/action
          [chat-icon]]]))))

(defn chat-toolbar []
  (let [{:keys [group-chat name contacts]}
        (subscribe [:chat-properties [:group-chat :name :contacts]])
        show-actions (subscribe [:show-actions])]
    (fn []
      [toolbar {:hide-nav?      @show-actions
                :custom-content [toolbar-content]
                :custom-action  [toolbar-action]}])))

(defview messages-view [group-chat]
  [messages [:chat :messages]
   contacts [:chat :contacts]]
  (let [contacts' (contacts-by-identity contacts)]
    [list-view {:renderRow                 (message-row contacts' group-chat)
                :renderScrollComponent     #(invertible-scroll-view (js->clj %))
                :onEndReached              #(dispatch [:load-more-messages])
                :enableEmptySections       true
                :keyboardShouldPersistTaps true
                :dataSource                (to-datasource messages)}]))

(defview chat []
  [group-chat [:chat :group-chat]
   show-actions-atom [:show-actions]
   command [:get-chat-command]
   to-msg-id [:get-chat-command-to-msg-id]]
  [view st/chat-view
   [chat-toolbar]
   [messages-view group-chat]
   (when group-chat [typing-all])
   (cond
     (and command to-msg-id) [request-view]
     command [content-suggestions-view]
     :else [suggestions-view])
   [chat-message-new]
   (when show-actions-atom [actions-view])])
