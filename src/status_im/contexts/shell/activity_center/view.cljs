(ns status-im.contexts.shell.activity-center.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.navigation :as navigation]
    [status-im.constants :as constants]
    [status-im.contexts.shell.activity-center.context :as ac.context]
    [status-im.contexts.shell.activity-center.header.view :as header]
    [status-im.contexts.shell.activity-center.notification-types :as types]
    [status-im.contexts.shell.activity-center.notification.admin.view :as admin]
    [status-im.contexts.shell.activity-center.notification.community-kicked.view :as
     community-kicked]
    [status-im.contexts.shell.activity-center.notification.community-request.view :as
     community-request]
    [status-im.contexts.shell.activity-center.notification.contact-requests.view :as
     contact-requests]
    [status-im.contexts.shell.activity-center.notification.contact-verification.view :as
     contact-verification]
    [status-im.contexts.shell.activity-center.notification.membership.view :as membership]
    [status-im.contexts.shell.activity-center.notification.mentions.view :as mentions]
    [status-im.contexts.shell.activity-center.notification.reply.view :as reply]
    [status-im.contexts.shell.activity-center.style :as style]
    [status-im.contexts.shell.activity-center.tabs.empty-tab.view :as empty-tab]
    [utils.re-frame :as rf]))

(defn notification-component
  [{notification-type :type :as notification} index]
  (let [extra-fn (rn/use-callback
                  (fn []
                    {:notification notification})
                  [notification])
        props    {:notification notification
                  :extra-fn     extra-fn}]
    [rn/view {:style (style/notification-container index)}
     (cond
       (= notification-type types/contact-verification)
       [contact-verification/view props]

       (= notification-type types/contact-request)
       [contact-requests/view props]

       (= notification-type types/mention)
       [mentions/view props]

       (= notification-type types/reply)
       [reply/view props]

       (= notification-type types/admin)
       [admin/view props]

       (types/membership notification-type)
       (condp = notification-type
         types/private-group-chat [membership/view props]
         types/community-request  [community-request/view props]
         types/community-kicked   [community-kicked/view props]
         nil))]))

(defn- fetch-next-page
  []
  (rf/dispatch [:activity-center.notifications/fetch-next-page]))

(defn view
  []
  (let [notifications    (rf/sub [:activity-center/notifications])

        ;; We globally control the active swipeable for all notifications
        ;; because when a swipe left/right gesture initiates, the previously
        ;; active swiped notification (if any) must be removed & closed with
        ;; animation.
        active-swipeable (rn/use-ref-atom nil)]
    (rn/use-mount
     (fn []
       (rf/dispatch [:activity-center.notifications/fetch-first-page])))

    [ac.context/provider {:active-swipeable active-swipeable}
     [quo/overlay {:type :shell}
      [rn/view {:style {:flex 1 :padding-top (navigation/status-bar-height)}}
       [header/header]
       (rn/delay-render
        [rn/flat-list
         {:data                      notifications
          :initial-num-to-render     constants/notifications-per-page
          :content-container-style   {:flex-grow 1}
          :empty-component           [empty-tab/empty-tab]
          :key-fn                    :id
          :on-scroll-to-index-failed identity
          :on-end-reached            fetch-next-page
          :render-fn                 notification-component}])]]]))
