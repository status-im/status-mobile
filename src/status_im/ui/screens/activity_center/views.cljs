(ns status-im.ui.screens.activity-center.views
  (:require [quo.react-native :as rn]
            [quo2.components.buttons.button :as button]
            [quo2.components.notifications.activity-logs :as activity-logs]
            [quo2.components.tags.context-tags :as context-tags]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.topbar :as topbar]
            [status-im.utils.handlers :refer [<sub >evt]]))

(defn render-notification
  [notification index]
  [rn/view {:flex           1
            :flex-direction :column
            :margin-top     (if (= 0 index) 0 4)}
   [activity-logs/activity-log {:context   [[context-tags/group-avatar-tag "Name" {:color          :purple
                                                                                   :override-theme :dark
                                                                                   :size           :small
                                                                                   :style          {:background-color colors/white-opa-10}
                                                                                   :text-style     {:color colors/white}}]
                                            [rn/text {:style {:color colors/white}} "did something here."]]
                                :icon      :placeholder
                                :message   {:body (get-in notification [:message :content :text])}
                                :timestamp (:timestamp notification)
                                :title     "Activity Title"
                                :unread?   (not (:read notification))}]])

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
