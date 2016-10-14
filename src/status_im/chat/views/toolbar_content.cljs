(ns status-im.chat.views.toolbar-content
  (:require [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as str]
            [cljs-time.core :as t]
            [status-im.components.react :refer [view
                                                text
                                                icon]]
            [status-im.i18n :refer [label label-pluralize]]
            [status-im.chat.styles.screen :as st]
            [status-im.components.refreshable-text.view :refer [refreshable-text]]
            [status-im.utils.datetime :as time]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.utils.gfycat.core :refer [generate-gfy]]
            [status-im.constants :refer [console-chat-id]]))

(defn online-text [contact chat-id]
  (cond
    (= chat-id console-chat-id) (label :t/available)
    contact (let [last-online      (get contact :last-online)
                  last-online-date (time/to-date last-online)
                  now-date         (t/now)]
              (if (and (> last-online 0)
                       (<= last-online-date now-date))
                (time/time-ago last-online-date)
                (label :t/active-unknown)))
    :else (label :t/active-unknown)))

(defn last-activity [{:keys [online-text sync-state]}]
  [refreshable-text {:style      st/last-activity
                     :text-style (get-in platform-specific [:component-styles :toolbar-last-activity])
                     :font       :default
                     :value      (case sync-state
                                   :in-progress (label :t/sync-in-progress)
                                   :synced (label :t/sync-synced)
                                   online-text)}])

(defn group-last-activity [{:keys [contacts sync-state]}]
  (if (or (= sync-state :in-progress)
          (= sync-state :synced))
    [last-activity {:sync-state sync-state}]
    [view {:flex-direction :row}
     [icon :group st/group-icon]
     [text {:style st/members
            :font  :medium}
      (let [cnt (inc (count contacts))]
        (label-pluralize cnt :t/members-active))]]))

(defn toolbar-content-view []
  (let [{:keys [group-chat
                name
                contacts
                chat-id]} (subscribe [:chat-properties [:group-chat :name :contacts :chat-id]])
        show-actions (subscribe [:chat-ui-props :show-actions?])
        contact      (subscribe [:get-in [:contacts @chat-id]])
        sync-state   (subscribe [:get :sync-state])]
    (fn []
      [view (st/chat-name-view @show-actions)
       [text {:style           st/chat-name-text
              :number-of-lines 1
              :font            :toolbar-title}
        (if (str/blank? @name)
          (generate-gfy)
          (or @name (label :t/chat-name)))]
       (if @group-chat
         [group-last-activity {:contacts   @contacts
                               :sync-state @sync-state}]
         [last-activity {:online-text (online-text @contact @chat-id)
                         :sync-state  @sync-state}])])))