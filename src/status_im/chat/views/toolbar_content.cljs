(ns status-im.chat.views.toolbar-content
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [cljs-time.core :as t]
            [status-im.ui.components.react :as react]

            [status-im.i18n :as i18n]
            [status-im.chat.views.photos :as photos]
            [status-im.chat.styles.screen :as st]
            [status-im.utils.datetime :as time]
            [status-im.utils.platform :refer [platform-specific]]
            [status-im.utils.gfycat.core :refer [generate-gfy]]
            [status-im.constants :refer [console-chat-id]]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.styles :as common.styles]))

(defn- online-text [contact chat-id]
  (cond
    (= console-chat-id chat-id) (i18n/label :t/available)
    contact (let [last-online      (get contact :last-online)
                  last-online-date (time/to-date last-online)
                  now-date         (t/now)]
              (if (and (pos? last-online)
                       (<= last-online-date now-date))
                (time/time-ago last-online-date)
                (i18n/label :t/active-unknown)))
    :else (i18n/label :t/active-unknown)))

(defn- in-progress-text [{:keys [highestBlock currentBlock startBlock]}]
  (let [total      (- highestBlock startBlock)
        ready      (- currentBlock startBlock)
        percentage (if (zero? ready)
                     0
                     (->> (/ ready total)
                          (* 100)
                          (.round js/Math)))]

    (str (i18n/label :t/sync-in-progress) " " percentage "% " currentBlock)))

(defview last-activity [{:keys [online-text sync-state accessibility-label]}]
  [state [:get :sync-data]]
  [react/text {:style               st/last-activity-text
               :accessibility-label accessibility-label}
   (case sync-state
     :in-progress (in-progress-text state)
     :synced      (i18n/label :t/sync-synced)
     online-text)])

(defn- group-last-activity [{:keys [contacts sync-state public?]}]
  (if (or (= sync-state :in-progress)
          (= sync-state :synced))
    [last-activity {:sync-state sync-state}]
    (if public?
      [react/view {:flex-direction :row}
       [react/text (i18n/label :t/public-group-status)]]
      [react/view {:flex-direction :row}
       [react/text {:style st/members}
        (if public?
          (i18n/label :t/public-group-status)
          (let [cnt (inc (count contacts))]
            (i18n/label-pluralize cnt :t/members-active)))]])))

(defview toolbar-content-view []
  (letsubs [{:keys [group-chat name contacts
                    public? chat-id]}             [:get-current-chat]
            show-actions?                         [:get-current-chat-ui-prop :show-actions?]
            accounts                              [:get-accounts]
            contact                               [:get-current-chat-contact]
            sync-state                            [:sync-state]]
    [react/view {:style st/toolbar-container}

     [react/view (when-not group-chat [photos/member-photo chat-id])]
     [react/view (st/chat-name-view (or (empty? accounts)
                                        show-actions?))
      (let [chat-name (if (string/blank? name)
                        (generate-gfy chat-id)
                        (or (i18n/get-contact-translated chat-id :name name)
                            (i18n/label :t/chat-name)))]
        [react/text {:style               st/chat-name-text
                     :number-of-lines     1
                     :font                :toolbar-title
                     :accessibility-label :chat-name-text}
         (if public?
           (str "#" chat-name)
           chat-name)])
      (if group-chat
        [group-last-activity {:contacts   contacts
                              :public?    public?
                              :sync-state sync-state}]
        [last-activity {:online-text         (online-text contact chat-id)
                        :sync-state          sync-state
                        :accessibility-label :last-seen-text}])]]))
