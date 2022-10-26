(ns status-im.ui.screens.activity-center.views
  (:require [quo.components.animated.pressable :as animation]
            [quo.react-native :as rn]
            [quo2.components.buttons.button :as button]
            [quo2.components.markdown.text :as text]
            [quo2.components.notifications.activity-logs :as activity-logs]
            [quo2.components.tabs.tabs :as tabs]
            [quo2.components.tags.context-tags :as context-tags]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [status-im.constants :as constants]
            [status-im.i18n.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.handlers :refer [<sub >evt]]
            [quo.components.safe-area :as safe-area]))

;;;; Misc

(defn sender-name
  [contact]
  (or (get-in contact [:names :nickname])
      (get-in contact [:names :three-words-name])))

(defmulti notification-component :type)

;;;; Contact request notifications

(defmethod notification-component constants/activity-center-notification-type-contact-request
  [{:keys [id] :as notification}]
  (let [message   (or (:message notification) (:last-message notification))
        contact   (<sub [:contacts/contact-by-identity (:author notification)])
        pressable (case (:contact-request-state message)
                    constants/contact-request-message-state-accepted
                    ;; NOTE [2022-09-21]: We need to dispatch to
                    ;; `:contact.ui/send-message-pressed` instead of
                    ;; `:chat.ui/navigate-to-chat`, otherwise the chat screen looks completely
                    ;; broken if it has never been opened before for the accepted contact.
                    [animation/pressable {:on-press (fn []
                                                      (>evt [:hide-popover])
                                                      (>evt [:contact.ui/send-message-pressed {:public-key (:author notification)}]))}]
                    [:<>])]
    (conj pressable
          [activity-logs/activity-log
           (merge {:title     (i18n/label :t/contact-request)
                   :icon      :main-icons2/add-user
                   :timestamp (datetime/timestamp->relative (:timestamp notification))
                   :unread?   (not (:read notification))
                   :context   [[context-tags/user-avatar-tag
                                {:color          :purple
                                 :override-theme :dark
                                 :size           :small
                                 :style          {:background-color colors/white-opa-10}
                                 :text-style     {:color colors/white}}
                                (sender-name contact)
                                (multiaccounts/displayed-photo contact)]
                               [rn/text {:style {:color colors/white}}
                                (i18n/label :t/contact-request-sent)]]
                   :message   {:body (get-in message [:content :text])}
                   :status    (case (:contact-request-state message)
                                constants/contact-request-message-state-accepted
                                {:type :positive :label (i18n/label :t/accepted)}
                                constants/contact-request-message-state-declined
                                {:type :negative :label (i18n/label :t/declined)}
                                nil)}
                  (case (:contact-request-state message)
                    constants/contact-request-state-mutual
                    {:button-1 {:label    (i18n/label :t/decline)
                                :type     :danger
                                :on-press #(>evt [:contact-requests.ui/decline-request id])}
                     :button-2 {:label                     (i18n/label :t/message-reply)
                                :type                      :success
                                :override-background-color colors/success-60
                                :on-press                  #(>evt [:contact-requests.ui/accept-request id])}}
                    nil))])))

;;;; Contact verification notifications

(defmethod notification-component constants/activity-center-notification-type-contact-verification
  [{:keys [id contact-verification-status] :as notification}]
  (let [message (or (:message notification) (:last-notification notification))
        contact (<sub [:contacts/contact-by-identity (:author notification)])]
    [activity-logs/activity-log
     (merge {:title     (i18n/label :t/identity-verification-request)
             :icon      :main-icons2/friend
             :timestamp (datetime/timestamp->relative (:timestamp notification))
             :unread?   (not (:read notification))
             :context   [[context-tags/user-avatar-tag
                          {:color          :purple
                           :override-theme :dark
                           :size           :small
                           :style          {:background-color colors/white-opa-10}
                           :text-style     {:color colors/white}}
                          (sender-name contact)
                          (multiaccounts/displayed-photo contact)]
                         [rn/text {:style {:color colors/white}}
                          (str (i18n/label :t/identity-verification-request-sent)
                               ":")]]
             :message   (case contact-verification-status
                          (constants/contact-verification-state-pending
                           constants/contact-verification-state-declined)
                          {:body (get-in message [:content :text])}
                          nil)
             :status    (case contact-verification-status
                          constants/contact-verification-state-declined
                          {:type :negative :label (i18n/label :t/declined)}
                          nil)}
            (case contact-verification-status
              constants/contact-verification-state-pending
              {:button-1 {:label    (i18n/label :t/decline)
                          :type     :danger
                          :on-press #(>evt [:activity-center.contact-verification/decline id])}
               :button-2 {:label    (i18n/label :t/accept)
                          :type     :primary
                          ;; TODO: The acceptance flow will be implemented in follow-up PRs.
                          :on-press identity}}
              nil))]))

;;;; Type-independent components

(defn render-notification
  [notification index]
  [rn/view {:margin-top         (if (= 0 index) 0 4)
            :padding-horizontal 20}
   [notification-component notification]])

(defn filter-selector-read-toggle
  []
  (let [unread-filter-enabled? (<sub [:activity-center/filter-status-unread-enabled?])]
    ;; TODO: Replace the button by a Filter Selector component once available for use.
    [button/button {:icon             true
                    :type             (if unread-filter-enabled? :primary :blur-bg-outline)
                    :size             32
                    :override-theme   :dark
                    :on-press         #(>evt [:activity-center.notifications/fetch-first-page
                                              {:filter-status (if unread-filter-enabled?
                                                                :read
                                                                :unread)}])}
     :main-icons2/unread]))

;; TODO(2022-10-07): The empty state is still under design analysis, so we
;; shouldn't even care about translations at this point. A placeholder box is
;; used instead of an image.
(defn empty-tab
  []
  [rn/view {:style {:align-items      :center
                    :flex             1
                    :justify-content  :center
                    :padding-vertical 12}}
   [rn/view {:style {:background-color colors/neutral-80
                     :height           120
                     :margin-bottom    20
                     :width            120}}]
   [text/text {:size   :paragraph-1
               :style  {:padding-bottom 2}
               :weight :semi-bold}
    "No notifications"]
   [text/text {:size :paragraph-2}
    "Your notifications will be here"]])

(defn tabs
  []
  (let [filter-type (<sub [:activity-center/filter-type])]
    [tabs/scrollable-tabs {:size                32
                           :blur?               true
                           :override-theme      :dark
                           :style               {:padding-left 20}
                           :fade-end-percentage 0.79
                           :scroll-on-press?    true
                           :fade-end?           true
                           :on-change           #(>evt [:activity-center.notifications/fetch-first-page {:filter-type %}])
                           :default-active      filter-type
                           :data                [{:id    constants/activity-center-notification-type-no-type
                                                  :label (i18n/label :t/all)}
                                                 {:id    constants/activity-center-notification-type-admin
                                                  :label (i18n/label :t/admin)}
                                                 {:id    constants/activity-center-notification-type-mention
                                                  :label (i18n/label :t/mentions)}
                                                 {:id    constants/activity-center-notification-type-reply
                                                  :label (i18n/label :t/replies)}
                                                 {:id    constants/activity-center-notification-type-contact-request
                                                  :label (i18n/label :t/contact-requests)}
                                                 {:id    constants/activity-center-notification-type-contact-verification
                                                  :label (i18n/label :t/identity-verification)}
                                                 {:id    constants/activity-center-notification-type-tx
                                                  :label (i18n/label :t/transactions)}
                                                 {:id    constants/activity-center-notification-type-membership
                                                  :label (i18n/label :t/membership)}
                                                 {:id    constants/activity-center-notification-type-system
                                                  :label (i18n/label :t/system)}]}]))

(defn header
  []
  (let [screen-padding 20]
    [rn/view
     [button/button {:icon           true
                     :type           :blur-bg
                     :size           32
                     :override-theme :dark
                     :style          {:margin-vertical  12
                                      :margin-left      screen-padding}
                     :on-press       #(>evt [:hide-popover])}
      :main-icons2/close]
     [text/text {:size   :heading-1
                 :weight :semi-bold
                 :style  {:padding-horizontal screen-padding
                          :padding-vertical   12
                          :color              colors/white}}
      (i18n/label :t/notifications)]
     [rn/view {:flex-direction   :row
               :padding-vertical 12}
      [rn/view {:flex       1
                :align-self :stretch}
       [tabs]]
      [rn/view {:flex-grow     0
                :margin-left   16
                :padding-right screen-padding}
       [filter-selector-read-toggle]]]]))

(defn activity-center
  []
  (reagent/create-class
   {:component-did-mount #(>evt [:activity-center.notifications/fetch-first-page])
    :reagent-render
    (fn []
      (let [notifications (<sub [:activity-center/filtered-notifications])
            window-width  (<sub [:dimensions/window-width])]
        [safe-area/view {:style {:flex 1}}
         [rn/view {:style {:width window-width
                           :flex  1}}
          [header]
          [rn/flat-list {:content-container-style {:flex-grow 1}
                         :data                    notifications
                         :empty-component         [empty-tab]
                         :key-fn                  :id
                         :on-end-reached          #(>evt [:activity-center.notifications/fetch-next-page])
                         :render-fn               render-notification}]]]))}))
