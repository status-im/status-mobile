(ns status-im.ui.screens.activity-center.views
  (:require [quo.react-native :as rn]
            [quo2.components.buttons.button :as button]
            [quo2.components.notifications.activity-logs :as activity-logs]
            [quo2.components.tags.context-tags :as context-tags]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [status-im.constants :as constants]
            [status-im.i18n.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.topbar :as topbar]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.handlers :refer [<sub >evt]]))

(defn activity-title
  [{:keys [type]}]
  (case type
    constants/activity-center-notification-type-contact-request
    (i18n/label :t/contact-request)

    constants/activity-center-notification-type-one-to-one-chat
    "Dummy 1:1 chat title"

    "Dummy default title"))

(defn activity-icon
  [{:keys [type]}]
  (case type
    constants/activity-center-notification-type-contact-request
    :add-user
    :placeholder))

(defn activity-context
  [{:keys [message last-message type]}]
  (case type
    constants/activity-center-notification-type-contact-request
    (let [message     (or message last-message)
          contact     (<sub [:contacts/contact-by-identity (:from message)])
          sender-name (or (get-in contact [:names :nickname])
                          (get-in contact [:names :three-words-name]))]
      [[context-tags/user-avatar-tag
        {:color          :purple
         :override-theme :dark
         :size           :small
         :style          {:background-color colors/white-opa-10}
         :text-style     {:color colors/white}}
        sender-name
        (multiaccounts/displayed-photo contact)]
       [rn/text {:style {:color colors/white}}
        (i18n/label :t/contact-request-sent)]])
    nil))

(defn activity-message
  [{:keys [message last-message]}]
  {:body (get-in (or message last-message) [:content :text])})

(defn activity-status
  [notification]
  (case (get-in notification [:message :contact-request-state])
    constants/contact-request-message-state-accepted
    {:type :positive :label (i18n/label :t/accepted)}
    constants/contact-request-message-state-declined
    {:type :negative :label (i18n/label :t/declined)}
    nil))

(defn activity-buttons
  [{:keys [type]}]
  (case type
    constants/activity-center-notification-type-contact-request
    {:button-1 {:label (i18n/label :t/decline)
                :type  :danger}
     :button-2 {:label (i18n/label :t/accept)
                :type  :success}}
    nil))

(defn render-notification
  [notification index]
  [rn/view {:flex           1
            :flex-direction :column
            :margin-top     (if (= 0 index) 0 4)}
   [activity-logs/activity-log
    (merge {:context   (activity-context notification)
            :icon      (activity-icon notification)
            :message   (activity-message notification)
            :status    (activity-status notification)
            :timestamp (datetime/timestamp->relative (:timestamp notification))
            :title     (activity-title notification)
            :unread?   (not (:read notification))}
           (activity-buttons notification))]])

(defn notifications-list
  []
  (let [notifications (<sub [:activity-center/notifications-per-read-status])]
    [rn/flat-list {:style          {:padding-horizontal 8}
                   :data           notifications
                   :key-fn         :id
                   :on-end-reached #(>evt [:activity-center/notifications-fetch-next-page])
                   :render-fn      render-notification}]))

(defn activity-center []
  (reagent/create-class
   {:component-did-mount #(>evt [:activity-center/notifications-fetch-first-page {:status-filter :unread}])
    :reagent-render
    (fn []
      [:<>
       [topbar/topbar {:navigation {:on-press #(>evt [:navigate-back])}
                       :title      (i18n/label :t/activity)}]
       ;; TODO(ilmotta): Temporary solution to switch between read/unread
       ;; notifications while the Design team works on the mockups.
       [button/button {:on-press #(>evt [:activity-center/notifications-fetch-first-page {:status-filter :unread}])}
        "Unread"]
       [button/button {:on-press #(>evt [:activity-center/notifications-fetch-first-page {:status-filter :read}])}
        "Read"]
       [notifications-list]])}))
