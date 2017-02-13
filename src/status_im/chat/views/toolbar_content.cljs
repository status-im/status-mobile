(ns status-im.chat.views.toolbar-content
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :as str]
            [cljs-time.core :as t]
            [status-im.components.react :refer [view
                                                text
                                                icon]]
            [status-im.i18n :refer [get-contact-translated
                                    label
                                    label-pluralize]]
            [status-im.chat.styles.screen :as st]
            [status-im.components.refreshable-text.view :refer [refreshable-text]]
            [status-im.utils.datetime :as time]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.utils.gfycat.core :refer [generate-gfy]]
            [status-im.constants :refer [console-chat-id wallet-chat-id]]))

(defn online-text [contact chat-id]
  (cond
    (#{console-chat-id wallet-chat-id} chat-id) (label :t/available)
    contact (let [last-online      (get contact :last-online)
                  last-online-date (time/to-date last-online)
                  now-date         (t/now)]
              (if (and (pos? last-online)
                       (<= last-online-date now-date))
                (time/time-ago last-online-date)
                (label :t/active-unknown)))
    :else (label :t/active-unknown)))

(defn in-progress-text [{:keys [highestBlock currentBlock startBlock]}]
  (let [total      (- highestBlock startBlock)
        ready      (- currentBlock startBlock)
        percentage (if (zero? ready)
                     0
                     (->> (/ ready total)
                          (* 100)
                          (.round js/Math)))]

    (str (label :t/sync-in-progress) " " percentage "% " currentBlock)))

(defview last-activity [{:keys [online-text sync-state]}]
  [state [:get :sync-data]]
  [refreshable-text {:style      st/last-activity
                     :text-style (get-in platform-specific [:component-styles :toolbar-last-activity])
                     :font       :default
                     :value      (case sync-state
                                   :in-progress (in-progress-text state)
                                   :synced (label :t/sync-synced)
                                   online-text)}])

(defn group-last-activity [{:keys [contacts sync-state public?]}]
  (if (or (= sync-state :in-progress)
          (= sync-state :synced))
    [last-activity {:sync-state sync-state}]
    (if public?
      [view {:flex-direction :row}
       [text {:font  :default
              :style (get-in platform-specific [:component-styles :toolbar-last-activity])}
        (label :t/public-group-status)]]
      [view {:flex-direction :row}
       [icon :group st/group-icon]
       [text {:style st/members
              :font  :medium}
        (if public?
          (label :t/public-group-status)
          (let [cnt (inc (count contacts))]
            (label-pluralize cnt :t/members-active)))]])))

(defn toolbar-content-view []
  (let [{:keys [group-chat
                name
                contacts
                chat-id
                public?]}
        (subscribe [:chat-properties [:group-chat :name :contacts :chat-id :public?]])
        show-actions? (subscribe [:chat-ui-props :show-actions?])
        accounts      (subscribe [:get :accounts])
        contact       (subscribe [:get-in [:contacts @chat-id]])
        sync-state    (subscribe [:get :sync-state])
        creating?     (subscribe [:get :creating-account?])]
    (fn []
      [view (st/chat-name-view (or (empty? @accounts)
                                   @show-actions?
                                   @creating?))
       (let [chat-name (if (str/blank? @name)
                         (generate-gfy)
                         (or (get-contact-translated @chat-id :name @name)
                             (label :t/chat-name)))]
         [text {:style           st/chat-name-text
                :number-of-lines 1
                :font            :toolbar-title}
          (if @public?
            (str "#" chat-name)
            chat-name)])
       (if @group-chat
         [group-last-activity {:contacts   @contacts
                               :public?    @public?
                               :sync-state @sync-state}]
         [last-activity {:online-text (online-text @contact @chat-id)
                         :sync-state  @sync-state}])])))
