(ns status-im.ui.screens.chat.toolbar-content
  (:require [cljs-time.core :as t]
            [status-im.i18n :as i18n]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.main :as st]
            [status-im.utils.datetime :as time])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn- online-text [contact chat-id]
  (if contact
    (let [last-online      (get contact :last-online)
          last-online-date (time/to-date last-online)
          now-date         (t/now)]
      (if (and (pos? last-online)
               (<= last-online-date now-date))
        (time/time-ago last-online-date)
        (i18n/label :t/active-unknown)))
    (i18n/label :t/active-unknown)))

(defn- in-progress-text [{:keys [highestBlock currentBlock startBlock]}]
  (let [total      (- highestBlock startBlock)
        ready      (- currentBlock startBlock)
        percentage (if (zero? ready)
                     0
                     (->> (/ ready total)
                          (* 100)
                          (.round js/Math)))]

    (str (i18n/label :t/sync-in-progress) " " percentage "% " currentBlock)))

(defview last-activity [{:keys [sync-state accessibility-label]}]
  (letsubs [state [:get :sync-data]]
    [react/text {:style               st/last-activity-text
                 :accessibility-label accessibility-label}
     (case sync-state
       :in-progress (in-progress-text state)
       :synced      (i18n/label :t/sync-synced))]))

(defn- group-last-activity [{:keys [contacts sync-state public?]}]
  (if (or (= sync-state :in-progress)
          (= sync-state :synced))
    [last-activity {:sync-state sync-state}]
    [react/view {:flex-direction :row}
     [react/text {:style st/toolbar-subtitle}
      (if public?
        (i18n/label :t/public-group-status)
        (let [cnt (count contacts)]
          (if (zero? cnt)
            (i18n/label :members-active-none)
            (i18n/label-pluralize cnt :t/members-active))))]]))

(defview toolbar-content-view []
  (letsubs [{:keys [group-chat color online contacts chat-name contact
                    public? chat-id] :as chat}    [:chats/current-chat]
            show-actions?                         [:chats/current-chat-ui-prop :show-actions?]
            accounts                              [:accounts/accounts]
            sync-state                            [:sync-state]]
    (let [has-subtitle? (or group-chat (not= :done sync-state))]
      [react/view {:style st/toolbar-container}
       [react/view {:margin-right 8}
        [chat-icon.screen/chat-icon-view-toolbar contact group-chat chat-name color online]]
       [react/view {:style st/chat-name-view}
        [react/text {:style               st/chat-name-text
                     :number-of-lines     1
                     :accessibility-label :chat-name-text}
         chat-name]
        (if group-chat
          [group-last-activity {:contacts   contacts
                                :public?    public?
                                :sync-state sync-state}]
          (when has-subtitle?
            [last-activity {:sync-state          sync-state
                            :accessibility-label :last-seen-text}]))]])))
